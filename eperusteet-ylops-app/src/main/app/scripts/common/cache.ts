
interface Cached<T> {
    ($q:IQService, resolve:(id:number, d:IDeferred<T>) => void)
    clear: (id?:number) => void
    get: (id:number) => IPromise<T>
    related: <E>(resolve:(from:T) => {[key:number]: E}) => Joined<E,T>
}
interface Joined<E,T> {
    ($q:IQService, parent:Cached<E>, resolve:(from:E) => {[key:number]: T})
    clear: () => void
    doClear: () => void
    get: (parentId:number, id:number) => IPromise<T>
    related: <Target>(resolve:(from:T) => {[key:number]: Target}) => SecondJoined<E,T,Target>
}
interface SecondJoined<F,E,T> {
    ($q:IQService, parent:Joined<F,E>, resolve:(from:E) => {[key:number]: T})
    clear: () => void
    doClear: () => void
    get: (rootId:number, parentId:number, id:number) => IPromise<T>
}

const secondJoined = <F,E,T>($q:IQService, parent:Joined<F,E>, resolve:(from:E) => {[key:number]: T}) => {
    var _q = $q;
    var _parent = parent;
    var _resolver = resolve;
    var _resolved:{[key:number]: {[key:number]: {[key:number]: T}}} = {};

    return {
        clear: () => {
            _parent.clear();
        },

        doClear: () => {
            _resolved = {};
        },

        get: (rootId:number, parentId:number, id:number) => {
            if (_resolved[rootId] && _resolved[rootId][parentId]) {
                return _q.when<T>(_.cloneDeep(_resolved[rootId][parentId][id]));
            }
            var d:IDeferred<T> = _q.defer<T>();
            _parent.get(rootId, parentId).then((parent:E) => {
                if (!_resolved[rootId]) {
                    _resolved[rootId] = {};
                }
                _resolved[rootId][parentId] = _resolver(parent);
                d.resolve(_.cloneDeep(_resolved[rootId][parentId][id]));
            });
            return d.promise;
        }
    };
};

const joined = <E,T>($q:IQService, parent:Cached<E>, resolve:(from:E) => {[key:number]: T}) => {
    var _q = $q;
    var _parent = parent;
    var _resolver = resolve;
    var _resolved:{[key:number]: {[key:number]: T}} = {};
    var _related:SecondJoined<E,T,any>[] = [];

    var self = {
        clear: () => {
            _parent.clear();
        },

        doClear: () => {
            _resolved = {};
            _.each(_related, (c:SecondJoined<E,T,any>) => c.doClear());
        },

        get: (parentId:number, id:number) => {
            if (_resolved[parentId]) {
                return _q.when<T>(_.cloneDeep(_resolved[parentId][id]));
            }
            var d:IDeferred<T> = _q.defer<T>();
            _parent.get(parentId).then((parent:E) => {
                _resolved[parentId] = _resolver(parent);
                d.resolve(_.cloneDeep(_resolved[parentId][id]));
            });
            return d.promise;
        },

        related: <Target>(resolve:(from:T) => {[key:number]: Target}) => {
            var j = secondJoined<E,T,Target>(_q, <Joined<E,T>>self, resolve);
            _related.push(<SecondJoined<E,T,any>>j);
            return j;
        }
    };
    return self;
};

const cached = <T>($q:IQService, resolve:(id:number, d:IDeferred<T>) => void) => {
    var _q = $q;
    var _resolve = resolve;
    var _cache:{ [id: number]: T} = {};
    var _related:Joined<T,any>[] = [];

    var self = {
        clear: (id?:number) => {
            if (id) {
                delete _cache[id];
            } else {
                _cache = {};
            }
            _.each(_related, (c:Joined<T,any>) => c.doClear());
        },

        get: (id:number) => {
            if (_cache[id]) {
                return _q.when<T>(_.cloneDeep(_cache[id]));
            }
            var d:IDeferred<T> = _q.defer<T>();
            d.promise.then((value:T) => _cache[id] = value);
            _resolve(id, d);
            return d.promise;
        },

        related: <E>(resolve:(from:T) => {[key:number]: E}) => {
            var j = joined<T,E>(_q, <Cached<T>>self, resolve);
            _related.push(j);
            return j;
        }
    };
    return self;
};
