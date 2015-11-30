
module Lokalisointi {
    function joinString(a: Lokalisoitu, b: Lokalisoitu, key?:string): string {
        if (!a[key] && !b[key]) {
            return null;
        }
        return (a[key] || '') + (b[key] || '');
    }
    function join(a: Lokalisoitu, b: Lokalisoitu) {
        var j: Lokalisoitu = {};
        j.fi = joinString(a,b,'fi');
        j.sv = joinString(a,b,'sv');
        j.en = joinString(a,b,'en');
        return j;
    }
    function forAll(constant: string): Lokalisoitu {
        return {
            fi: constant,
            sv: constant,
            en: constant
        };
    }
    export function concat(...a: (Lokalisoitu | string)[]): Lokalisoitu {
        if (a.length == 0) {
            return forAll(null);
        }
        var j: Lokalisoitu = a[0];
        for (var i = 1; i < a.length; ++i) {
            j = join(j, a[i]);
        }
        return j;
    }
}


