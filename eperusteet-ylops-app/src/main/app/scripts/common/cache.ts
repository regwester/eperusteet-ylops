module Cache {
    export class Cached<T> {
        private _cache: { [id: number]: T} = {};
        private _q: IQService;
        private _related: Joined<T,any>[] = [];
        private _resolve: (id: number, d: IDeferred<T>) => void = null;

        constructor($q:IQService, resolve: (id: number, d: IDeferred<T>) => void) {
            this._q = $q;
            this._resolve = resolve;
        }

        clear(id?: number) {
            if (id) {
                delete this._cache[id];
            } else {
                this._cache = {};
            }
            _.each(this._related, function(c: Joined<T,any>) {
                c.doClear();
            });
            return this;
        }

        get(id: number) {
            if (this._cache[id]) {
                return this._q.when<T>(_.cloneDeep(this._cache[id]));
            }
            var d:IDeferred<T> = this._q.defer<T>();
            d.promise.then((value: T) => this._cache[id] = value);
            this._resolve(id, d);
            return d.promise;
        }

        related<E>(resolve: (from: T) => {[key:number]: E}) {
            var joined = new Joined<T,E>(this._q, this, resolve);
            this._related.push(joined);
            return joined;
        }
    }

    export class Joined<E,T> {
        protected _q: IQService;
        protected _parent: Cached<E> = null;
        protected _resolver: (from: E) => {[key:number]: T} = null;
        protected _resolved: {[key:number]: {[key:number]: T}} = {};
        protected _related: SecondJoined<E,T,any>[] = [];

        constructor($q:IQService, parent: Cached<E>, resolve: (from: E) => {[key:number]: T}) {
            this._q = $q;
            this._parent = parent;
            this._resolver = resolve;
        }

        clear() {
            this._parent.clear();
            return this;
        }

        doClear():void {
            this._resolved = {};
            _.each(this._related, function(c: SecondJoined<E,T,any>) {
                c.doClear();
            });
        }

        get(parentId: number, id: number) {
            if (this._resolved[parentId]) {
                return this._q.when<T>(_.cloneDeep(this._resolved[parentId][id]));
            }
            var d:IDeferred<T> = this._q.defer<T>();
            this._parent.get(parentId).then((parent: E) => {
                this._resolved[parentId] = this._resolver(parent);
                d.resolve(_.cloneDeep(this._resolved[parentId][id]));
            });
            return d.promise;
        }

        related<Target>(resolve: (from: T) => {[key:number]: Target}) {
            var joined = new SecondJoined<E,T,Target>(this._q, this, resolve);
            this._related.push(joined);
            return joined;
        }
    }

    export class SecondJoined<F,E,T> {
        protected _q: IQService;
        protected _parent: Joined<F,E> = null;
        protected _resolver: (from: E) => {[key:number]: T} = null;
        protected _resolved: {[key:number]: {[key:number]: {[key:number]: T}}} = {};

        constructor($q:IQService, parent: Joined<F,E>, resolve: (from: E) => {[key:number]: T}) {
            this._q = $q;
            this._parent = parent;
            this._resolver = resolve;
        }

        clear() {
            this._parent.clear();
            return this;
        }

        doClear():void {
            this._resolved = {};
        }

        get(rootId: number, parentId: number, id: number) {
            if (this._resolved[rootId] && this._resolved[rootId][parentId]) {
                return this._q.when<T>(_.cloneDeep(this._resolved[rootId][parentId][id]));
            }
            var d:IDeferred<T> = this._q.defer<T>();
            this._parent.get(rootId, parentId).then((parent: E) => {
                if (!this._resolved[rootId]) {
                    this._resolved[rootId] = {};
                }
                this._resolved[rootId][parentId] = this._resolver(parent);
                d.resolve(_.cloneDeep(this._resolved[rootId][parentId][id]));
            });
            return d.promise;
        }
    }
}