import {LocalStorageMechanism, Storage} from 'metal-storage';
import middlewares from './middlewares/defaults';

// Gateway

import Client from './client';
import defaultPlugins from './plugins/defaults';
import hash from './utils/hash';
import uuidv1 from 'uuid/v1';

// Constants

const ENV = window || global;

const FLUSH_INTERVAL = 2000;

const REQUEST_TIMEOUT = 5000;

// Local Storage keys

const STORAGE_KEY_CONTEXTS = 'ac_client_context';

const STORAGE_KEY_EVENTS = 'ac_client_batch';

const STORAGE_KEY_IDENTITY_HASH = 'ac_client_identity';

const STORAGE_KEY_USER_ID = 'ac_client_user_id';

// Creates LocalStorage wrapper

const storage = new Storage(new LocalStorageMechanism());

let instance;

/**
 * Analytics class that is desined to collect events that are captured
 * for later processing. It persists the events in the LocalStorage using the
 * metal-storage implementation and flushes it to the defined endpoint at
 * regular intervals.
 */
class Analytics {

	/**
	 * Returns an Analytics instance and triggers the automatic flush loop
	 * @param {object} config object to instantiate the Analytics tool
	 */
	constructor(config) {
		if (!instance) {
			instance = this;
		}

		const {endpointUrl, flushInterval} = config;

		const client = new Client(endpointUrl);

		instance.client = client;

		instance._sendData = userId => {
			return client.send(instance, userId);
		};

		instance.config = config;

		instance.identityEndpoint = `${endpointUrl}/identity`;

		instance.events = storage.get(STORAGE_KEY_EVENTS) || [];
		instance.contexts = storage.get(STORAGE_KEY_CONTEXTS) || [];
		instance.isFlushInProgress = false;

		// Initializes default plugins

		instance._pluginDisposers = defaultPlugins.map(plugin => plugin(instance));

		// Starts flush loop

		if (instance.flushInterval) {
			clearInterval(instance.flushInterval);
		}

		instance.flushInterval = setInterval(
			() => instance.flush(),
			flushInterval || FLUSH_INTERVAL
		);

		this._ensureIntegrity();

		return instance;
	}

	/**
	 * Clears interval and calls plugins disposers if available
	 */
	disposeInternal() {
		if (this.flushInterval) {
			clearInterval(this.flushInterval);
		}

		instance._pluginDisposers
			.filter(disposer => typeof disposer === 'function')
			.forEach(disposer => disposer());
	}

	_ensureIntegrity() {
		let userId = storage.get(STORAGE_KEY_USER_ID);

		if (userId) {
			this._setCookie(STORAGE_KEY_USER_ID, userId);
		}
	}
	
	_isNewUserIdRequired() {
		const identityHash = storage.get(STORAGE_KEY_IDENTITY_HASH);
		const storedUserId = storage.get(STORAGE_KEY_USER_ID);

		let newUserIdRequired = false;

		// During logout or session expiration, identiy object becomes undefined
		// because the client object is being instatiated on every page navigation,
		// in such cases, we force a new user ID token.

		if (!storedUserId || (identityHash && !this.config.identity)) {
			newUserIdRequired = true;
		}

		return newUserIdRequired;
	}

	/**
	 * Persists the event queue to the LocalStorage
	 * @protected
	 */
	_persist(key, data) {
		storage.set(key, data);

		return data;
	}

	/**
	 * Serializes data and returns the result appending a timestamp to the returned
	 * data as well
	 * @param {string} eventId The event Id
	 * @param {string} applicationId The application Id
	 * @param {object} properties Additional properties to serialize
	 * @protected
	 * @return {object}
	 */
	_serialize(eventId, applicationId, properties, contextHash) {
		const eventDate = new Date().toISOString();

		return {
			applicationId,
			contextHash,
			eventDate,
			eventId,
			properties
		};
	}

	/**
	 * Sets a browser cookie
	 * @protected
	 */
	_setCookie(key, data) {
		const expirationDate = new Date();

		expirationDate.setDate(expirationDate.getDate() + 365);

		document.cookie = key + "=" + data + "; expires=" + expirationDate.toUTCString() + " ;path=/";
	}

	/**
	 * Returns a promise that times out after the given time limit is exceeded
	 * @param {number} timeout
	 * @return {object} Promise
	 */
	_timeout(timeout) {
		return new Promise((resolve, reject) => setTimeout(reject, timeout));
	}

	/**
	 * Returns a unique identifier for an event
	 * @param {object} The event
	 * @return {string} The generated id
	 */
	_getEventKey({applicationId, eventDate, eventId}) {
		return `${applicationId}${eventDate}${eventId}`;
	}

	/**
	 * Returns an unique identifier for an user, additionaly it stores
	 * the generated token to the local storage cache and clears
	 * previously stored identiy hash.
	 * @return {string} The generated id
	 */
	_generateUserId() {
		const userId = uuidv1();

		this._persist(STORAGE_KEY_USER_ID, userId);
		this._setCookie(STORAGE_KEY_USER_ID, userId);

		storage.remove(STORAGE_KEY_IDENTITY_HASH);

		return userId;
	}

	_getContext() {
		const {context} = middlewares.reduce(
			(request, middleware) => middleware(request, this),
			{context: {}}
		);

		return context;
	}

	/**
	 * Gets the userId for the existing analytics user. Previously generated ids
	 * are stored and retrieved before generating a new one. If a anonymous
	 * navigation is started after a identified navigation, the user ID token
	 * is regenerated.
	 * @return {Promise} A promise resolved with the stored or generated userId
	 */
	_getUserId() {
		const newUserIdRequired = this._isNewUserIdRequired();

		let userId = Promise.resolve(storage.get(STORAGE_KEY_USER_ID));

		if (newUserIdRequired) {
			userId = Promise.resolve(this._generateUserId());
		}

		return userId;
	}

	/**
	 * Sends the identity information and user id to the Identity Service.
	 * @param {Object} identity The identity information about an user.
	 * @param {String} userId The unique user id.
	 * @return {Promise} A promise returned by the fetch request.
	 */
	_sendIdentity(identity, userId) {
		const {dataSourceId} = this.config;

		const bodyData = {
			dataSourceId,
			identity,
			userId
		};

		const newIdentityHash = hash(bodyData);
		const storedIdentityHash = storage.get(STORAGE_KEY_IDENTITY_HASH);

		let identyHash = Promise.resolve(storedIdentityHash);

		if (newIdentityHash !== storedIdentityHash) {
			instance._persist(STORAGE_KEY_IDENTITY_HASH, newIdentityHash);

			const body = JSON.stringify(bodyData);
			const headers = new Headers();

			headers.append('Content-Type', 'application/json');

			const request = {
				body,
				cache: 'default',
				credentials: 'same-origin',
				headers,
				method: 'POST',
				mode: 'cors'
			};

			identyHash = fetch(this.identityEndpoint, request).then(
				() => newIdentityHash
			);
		}

		return identyHash;
	}

	/**
	 * Flushes the event queue and sends it to the registered endpoint. If no data
	 * is pending or a flush is already in progress, the request will be ignored.
	 * @return {object} A Promise that resolves successfully when the data has been
	 * sent or no pending data was left to be sent
	 */
	flush() {
		let result;

		if (!this.isFlushInProgress && this.events.length) {
			this.isFlushInProgress = true;

			const eventKeys = this.events
				.map(event => this._getEventKey(event));

			result = Promise.race(
				[
					this._getUserId().then(userId => instance._sendData(userId)),
					this._timeout(REQUEST_TIMEOUT)
				]
			).then(
				() => {
					const events = this.events.filter(
						event =>
							eventKeys.indexOf(this._getEventKey(event)) > -1
					);

					this.reset(events);
				}
			).catch().then(() => (this.isFlushInProgress = false));
		}
		else {
			result = Promise.resolve();
		}

		return result;
	}

	/**
	 * Registers the given plugin and executes its initialistion logic
	 * @param {Function} plugin An Analytics Plugin
	 */
	registerPlugin(plugin) {
		if (typeof plugin === 'function') {
			plugin(this);
		}
	}

	/**
	 * Registers the given middleware. This middleware will be later on called
	 * with the request object and this Analytics instance
	 * @param {Function} middleware A function that will be invoked on every request
	 * @example
	 * Analytics.registerMiddleware(
	 *   (request, analytics) => {
	 *     ...
	 *   }
	 * );
	 */
	registerMiddleware(middleware) {
		if (typeof middleware === 'function') {
			this.client.use(middleware);
		}
	}

	/**
	 * Resets the event queue
	 * @param {Array} A list of events to be cleared from the queue. If not passed,
	 * clears everything.
	 */
	reset(events) {
		if (events) {
			this.events = this.events.filter(
				event => {
					const eventKey = this._getEventKey(event);

					return !events.find(
						evt => {
							return this._getEventKey(evt) === eventKey;
						}
					);
				}
			);
		}
		else {
			this.events.length = 0;
		}

		if (!this.events.length) {
			const context = this._getContext();

			this.contexts = this.contexts.filter(
				storedContext => hash(context) == hash(storedContext)
			);
		}

		this._persist(STORAGE_KEY_EVENTS, this.events);
		this._persist(STORAGE_KEY_CONTEXTS, this.contexts);
	}

	/**
	 * Registers an event that is to be sent to the LCS endpoint
	 * @param {string} eventId Id of the event
	 * @param {string} applicationId ID of the application that triggered the event
	 * @param {object} eventProps Complementary information about the event
	 */
	send(eventId, applicationId, eventProps) {
		const currentContext = this._getContext();
		const currentContextHash = hash(currentContext);

		const hasStoredContext = this.contexts.find(
			storedContext => hash(storedContext) === currentContextHash
		);

		if (!hasStoredContext) {
			this.contexts = [...this.contexts, currentContext];
		}

		this.events = [
			...this.events,
			this._serialize(
				eventId,
				applicationId,
				eventProps,
				currentContextHash
			)
		];

		this._persist(STORAGE_KEY_EVENTS, this.events);
		this._persist(STORAGE_KEY_CONTEXTS, this.contexts);
	}

	/**
	 * Sets the current user identity in the system. This is meant to be invoked
	 * by consumers every time an identity change is detected. If the identity is
	 * different than the previously stored one, we will save this new identity and
	 * send a request updating the Identity Service.
	 * @param {object} identity A key-value pair object that identifies the user
	 * @return {Promise} A promise resolved with the generated identity hash
	 */
	setIdentity(identity) {
		this.config.identity = identity;

		return this._getUserId().then(userId => this._sendIdentity(identity, userId));
	}

	/**
	 * Creates a singleton instance of Analytics
	 * @param {object} config Configuration object
	 * @example
	 * Analytics.create(
	 *   {
	 *	   endpointUrl: 'https://osbasahcerebropublisher-projectname.lfr.io'
	 *	   flushInterval: 2000,
	 *	   userId: 'id-s7uatimmxgo',
	 *     dataSourceId: 'MyDataSourceId',
	 *   }
	 * );
	 */
	static create(config = {}) {
		const self = new Analytics(config);

		ENV.Analytics = self;
		ENV.Analytics.create = Analytics.create;
		ENV.Analytics.dispose = Analytics.dispose;

		return self;
	}

	/**
	 * Disposes events and stops interval timer
	 * @example
	 * Analytics.dispose();
	 */
	static dispose() {
		const self = ENV.Analytics;

		if (self) {
			self.disposeInternal();
		}
	}
}

// Exposes Analytics.create to the global scope

ENV.Analytics = {
	create: Analytics.create
};

export {Analytics};
export default Analytics;