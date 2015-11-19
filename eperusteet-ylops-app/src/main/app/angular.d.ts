
declare module angular {
    interface IQResolveReject<T> {
        (): void;
        (value: T): void;
    }

    /**
     * $q - service in module ng
     * A promise/deferred implementation inspired by Kris Kowal's Q.
     * See http://docs.angularjs.org/api/ng/service/$q
     */
    interface IQService {
        new <T>(resolver: (resolve: IQResolveReject<T>) => any): IPromise<T>;
        new <T>(resolver: (resolve: IQResolveReject<T>, reject: IQResolveReject<any>) => any): IPromise<T>;
        <T>(resolver: (resolve: IQResolveReject<T>) => any): IPromise<T>;
        <T>(resolver: (resolve: IQResolveReject<T>, reject: IQResolveReject<any>) => any): IPromise<T>;

        /**
         * Combines multiple promises into a single promise that is resolved when all of the input promises are resolved.
         *
         * Returns a single promise that will be resolved with an array of values, each value corresponding to the promise at the same index in the promises array. If any of the promises is resolved with a rejection, this resulting promise will be rejected with the same rejection value.
         *
         * @param promises An array of promises.
         */
        all<T>(promises: IPromise<any>[]): IPromise<T[]>;
        /**
         * Combines multiple promises into a single promise that is resolved when all of the input promises are resolved.
         *
         * Returns a single promise that will be resolved with a hash of values, each value corresponding to the promise at the same key in the promises hash. If any of the promises is resolved with a rejection, this resulting promise will be rejected with the same rejection value.
         *
         * @param promises A hash of promises.
         */
        all(promises: { [id: string]: IPromise<any>; }): IPromise<{ [id: string]: any; }>;
        all<T extends {}>(promises: { [id: string]: IPromise<any>; }): IPromise<T>;
        /**
         * Creates a Deferred object which represents a task which will finish in the future.
         */
        defer<T>(): IDeferred<T>;
        /**
         * Creates a promise that is resolved as rejected with the specified reason. This api should be used to forward rejection in a chain of promises. If you are dealing with the last promise in a promise chain, you don't need to worry about it.
         *
         * When comparing deferreds/promises to the familiar behavior of try/catch/throw, think of reject as the throw keyword in JavaScript. This also means that if you "catch" an error via a promise error callback and you want to forward the error to the promise derived from the current promise, you have to "rethrow" the error by returning a rejection constructed via reject.
         *
         * @param reason Constant, message, exception or an object representing the rejection reason.
         */
        reject(reason?: any): IPromise<any>;
        /**
         * Wraps an object that might be a value or a (3rd party) then-able promise into a $q promise. This is useful when you are dealing with an object that might or might not be a promise, or if the promise comes from a source that can't be trusted.
         *
         * @param value Value or a promise
         */
        resolve<T>(value: IPromise<T>|T): IPromise<T>;
        /**
         * Wraps an object that might be a value or a (3rd party) then-able promise into a $q promise. This is useful when you are dealing with an object that might or might not be a promise, or if the promise comes from a source that can't be trusted.
         */
        resolve(): IPromise<void>;
        /**
         * Wraps an object that might be a value or a (3rd party) then-able promise into a $q promise. This is useful when you are dealing with an object that might or might not be a promise, or if the promise comes from a source that can't be trusted.
         *
         * @param value Value or a promise
         */
        when<T>(value: IPromise<T>|T): IPromise<T>;
        /**
         * Wraps an object that might be a value or a (3rd party) then-able promise into a $q promise. This is useful when you are dealing with an object that might or might not be a promise, or if the promise comes from a source that can't be trusted.
         */
        when(): IPromise<void>;
    }

    interface IPromise<T> {
        /**
         * Regardless of when the promise was or will be resolved or rejected, then calls one of the success or error callbacks asynchronously as soon as the result is available. The callbacks are called with a single argument: the result or rejection reason. Additionally, the notify callback may be called zero or more times to provide a progress indication, before the promise is resolved or rejected.
         * The successCallBack may return IPromise<void> for when a $q.reject() needs to be returned
         * This method returns a new promise which is resolved or rejected via the return value of the successCallback, errorCallback. It also notifies via the return value of the notifyCallback method. The promise can not be resolved or rejected from the notifyCallback method.
         */
        then<TResult>(successCallback: (promiseValue: T) => IPromise<TResult>|TResult, errorCallback?: (reason: any) => any, notifyCallback?: (state: any) => any): IPromise<TResult>;

        /**
         * Shorthand for promise.then(null, errorCallback)
         */
        catch<TResult>(onRejected: (reason: any) => IPromise<TResult>|TResult): IPromise<TResult>;

        /**
         * Allows you to observe either the fulfillment or rejection of a promise, but to do so without modifying the final value. This is useful to release resources or do some clean-up that needs to be done whether the promise was rejected or resolved. See the full specification for more information.
         *
         * Because finally is a reserved word in JavaScript and reserved keywords are not supported as property names by ES3, you'll need to invoke the method like promise['finally'](callback) to make your code IE8 and Android 2.x compatible.
         */
        finally(finallyCallback: () => any): IPromise<T>;
    }

    interface IDeferred<T> {
        resolve(value?: T): void;
        reject(reason?: any): void;
        notify(state?: any): void;
        promise: IPromise<T>;
    }
}