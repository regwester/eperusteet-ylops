namespace Lokalisointi {
    function joinString(a: l.Lokalisoitu, b: l.Lokalisoitu, key?: string): string {
        if (!a[key] && !b[key]) {
            return null;
        }
        return (a[key] || "") + (b[key] || "");
    }

    function join(a: l.Lokalisoitu, b: l.Lokalisoitu) {
        return <l.Lokalisoitu>{
            fi: joinString(a, b, "fi"),
            sv: joinString(a, b, "sv"),
            en: joinString(a, b, "en")
        };
    }

    function forAll(constant: string): l.Lokalisoitu {
        return {
            fi: constant,
            sv: constant,
            en: constant
        };
    }

    export function concat(...a: (l.Lokalisoitu | string)[]): Lokalisoitu {
        if (a.length == 0) {
            return forAll(null);
        }
        var j = _.isObject(a[0]) ? <l.Lokalisoitu>a[0] : forAll(<string>a[0]);
        for (var i = 1; i < a.length; ++i) {
            if (a[i]) {
                j = join(j, _.isObject(a[i]) ? <l.Lokalisoitu>a[i] : forAll(<string>a[i]));
            }
        }
        return j;
    }
}
