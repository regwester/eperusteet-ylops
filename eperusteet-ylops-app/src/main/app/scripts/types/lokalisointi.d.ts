
declare module Lokalisointi {
    export interface Lokalisoitu {
        fi?: string;
        sv?: string;
        en?: string;
    }

    export interface TekstiOsa {
        otsikko: Lokalisoitu;
        teksti?: Lokalisoitu;
    }
}

import l = Lokalisointi;