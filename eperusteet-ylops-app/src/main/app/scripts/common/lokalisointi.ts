
module Lokalisointi {
    function joinString(a:Lokalisoitu, b:Lokalisoitu, key?:string):string {
        if (!a[key] && !b[key]) {
            return null;
        }
        return (a[key] || '') + (b[key] || '');
    }

    function join(a:Lokalisoitu, b:Lokalisoitu) {
        return <Lokalisoitu>{
            fi: joinString(a, b, 'fi'),
            sv: joinString(a, b, 'sv'),
            en: joinString(a, b, 'en')
        };
    }

    function forAll(constant:string):Lokalisoitu {
        return {
            fi: constant,
            sv: constant,
            en: constant
        };
    }

    export function concat(...a:(Lokalisoitu | string)[]):Lokalisoitu {
        if (a.length == 0) {
            return forAll(null);
        }
        var j:Lokalisoitu = a[0];
        for (var i = 1; i < a.length; ++i) {
            if (a[i]) {
                j = join(j, _.isObject(a[i]) ? a[i] : forAll(a[i]));
            }
        }
        return j;
    }
}


