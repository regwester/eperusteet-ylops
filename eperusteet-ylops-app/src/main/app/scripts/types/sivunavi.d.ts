declare namespace Sivunavi {
    export interface NavigaatioItem {
        label: string | l.Lokalisoitu; // lokalisaatioavain
        url: string; // URL (provided by e.g. $state.href(...))
        depth?: number;
        active?: boolean;
        valmis?: boolean;
    }
}

import sn = Sivunavi;
