
declare module Lukio {
    export interface IdHolder {
        id: number
    }

    export enum Tila {
        luonnos,
        valmis,
        poistettu,
        julkaistu
    }

    export interface PerusteOpsOsa<Perusteen,Opsin> {
        perusteen?: Perusteen;
        paikallinen: Opsin;
        tunniste?: string;
        kommentti?: string;
    }

    export interface Aihekokonaisuus {
        tunniste?: string;
        id?: number;
        jnro?: number;
        otsikko?: l.Lokalisoitu;
        yleiskuvaus?: l.Lokalisoitu;
    }

    export interface AihekokonaisuudetPaivitaYleiskuvaus {
        otsikko: l.Lokalisoitu;
        yleiskuvaus: l.Lokalisoitu;
    }

    export interface LuoAihekokonaisuus {
        otsikko: l.Lokalisoitu;
        yleiskuvaus: l.Lokalisoitu;
    }
    export interface PaivitaAihekokonaisuus extends LuoAihekokonaisuus{
    }

    export interface JarjestaAihekokonaisuudet {
        aihekokonaisuudet: IdHolder[]
    }

    export interface MuokkaaAihekokonaisuutta {
        id: number;
        otsikko: l.Lokalisoitu;
        yleiskuvaus: l.Lokalisoitu;
    }

    export interface Aihekokonaisuudet {
        uuidTunniste?: string;
        id? : number;
        otsikko?: l.Lokalisoitu;
        yleiskuvaus?: l.Lokalisoitu;
        aihekokonaisuudet: Aihekokonaisuus[];
    }

    export interface OpsAihekokonaisuus extends Aihekokonaisuus {
        perusteen?: Aihekokonaisuus;
        parent?: OpsAihekokonaisuus;
    }

    export interface OpsAihekokonaisuudet extends Aihekokonaisuudet {
        aihekokonaisuudet: OpsAihekokonaisuus[];
    }

    export interface OpetuksenYleisetTavoitteet {
        uuidTunniste?: string;
        id?: number;
        otsikko?: l.Lokalisoitu;
        kuvaus?: l.Lokalisoitu;
    }

    export interface OpsOpetuksenYleisetTavoitteet extends OpetuksenYleisetTavoitteet {
        perusteen?: OpetuksenYleisetTavoitteet
    }

    export interface AihekokonaisuudetPerusteenOsa extends PerusteOpsOsa<Aihekokonaisuudet,
        OpsAihekokonaisuudet> {
    }
    export interface OpetuksenYleisetTavoitteetPerusteenOsa extends PerusteOpsOsa<OpetuksenYleisetTavoitteet,
        OpsOpetuksenYleisetTavoitteet> {
    }

    export enum LukioKurssiTyyppiPeruste {
        PAKOLLINEN,
        VALTAKUNNALLINEN_SYVENTAVA,
        VALTAKUNNALLINEN_SOVELTAVA
    }

    export enum LukioKurssiTyyppi {
        VALTAKUNNALLINEN_PAKOLLINEN,
        VALTAKUNNALLINEN_SYVENTAVA,
        VALTAKUNNALLINEN_SOVELTAVA,
        PAIKALLINEN_SYVENTAVA,
        PAIKALLINEN_SOVELTAVA
    }

    export enum OppiaineTyyppi {
        yhteinen,
        taide_taitoaine,
        muu_valinnainen
    }

    export interface Kurssi {
        id?: number;
        tunniste?: string;
        nimi: l.Lokalisoitu;
        kuvaus?: l.Lokalisoitu;
        koodiUri?: string;
        koodiArvo?: string;
        lokalisoituKoodi?: l.Lokalisoitu;
    }

    export interface LukiokurssiPeruste extends Kurssi {
        oppiaineId: number;
        jarjestys?: number;
        tyyppi: LukioKurssiTyyppiPeruste;
        tavoitteet?: l.TekstiOsa;
        keskeinenSisalto?: l.TekstiOsa;
        tavoitteetJaKeskeinenSisalto?: l.TekstiOsa;
    }

    export interface LukiokurssiOps extends Kurssi {
        oppiaineId: number;
        jarjestys?: number;
        palautettava: boolean;
        tyyppi: LukioKurssiTyyppi;
        tavoitteet?: l.TekstiOsa;
        laajuus: number,
        keskeinenSisalto?: l.TekstiOsa;
        tavoitteetJaKeskeinenSisalto?: l.TekstiOsa;
    }

    export interface LukiokurssiPerusTiedot {
        nimi: l.Lokalisoitu;
        kuvaus?: l.Lokalisoitu;
        koodiUri?: string;
        koodiArvo?: string;
        lokalisoituKoodi?: l.Lokalisoitu;
        tyyppi: string /**paikallinen LukioKurssiTyyppi**/;
        tavoitteet?: l.TekstiOsa;
        keskeinenSisalto?: l.TekstiOsa;
        tavoitteetJaKeskeinenSisalto?: l.TekstiOsa;
    }

    export interface LuoLukiokurssi extends LukiokurssiPerusTiedot {
        oppiaineId: number
    }

    export interface UpdateLukiokurssi extends LukiokurssiPerusTiedot {
    }

    export interface Oppiaine {
        koodiUri?: string;
        koodiArvo?: string;
        koosteinen: boolean;
        nimi: l.Lokalisoitu;
        kuvaus?: l.Lokalisoitu;
        tehtava?: l.TekstiOsa;
        tavoitteet?: l.TekstiOsa;
        arviointi?: l.TekstiOsa;
    }

    export interface AbstraktiOppiaine {
        tunniste: string
        nimi: l.Lokalisoitu
    }

    export interface OppiaineKielitarjonta {
        tunniste: string
        nimi: l.Lokalisoitu
        kieliKoodiUri?: string
        kieliKoodiArvo?: string
        kieli: l.Lokalisoitu
    }

    export interface LukioOppiaineTallennus extends Oppiaine {
        laajuus?: string;
        oppiaineId?: number;
        kurssiTyyppiKuvaukset: { [key:string/*LukioKurssiTyyppi, not supported in ts*/]: l.Lokalisoitu; };
    }

    export interface LukioOppiainePeruste extends Oppiaine {
        id: number;
        tunniste?: string;
        jarjestys?: number;
        abstrakti?: boolean;
        pakollinenKurssiKuvaus?: l.Lokalisoitu;
        syventavaKurssiKuvaus?: l.Lokalisoitu;
        soveltavaKurssiKuvaus?: l.Lokalisoitu;
        oppimaarat?: LukioOppiainePeruste[];
        kurssit?: LukiokurssiPeruste[];
    }

    export interface LukioOpetussuunnitelmaRakennePeruste {
        perusteId: number;
        oppiaineet: LukioOppiainePeruste[];
    }

    export interface LukioOppiaine extends Oppiaine {
        id: number;
        tunniste?: string;
        perusteen?: LukioOppiainePeruste;
        oppiaineId?: number;
        tila: Tila;
        oma: boolean;
        maariteltyPohjassa: boolean;
        abstrakti?: boolean;
        jarjestys?: number;
        tyyppi: OppiaineTyyppi;
        laajuus: string;
        kurssiTyyppiKuvaukset: { [key:string/*LukioKurssiTyyppi, not supported in ts*/]: l.Lokalisoitu; };
        oppimaarat?: LukioOppiaine[];
        kurssit: LukiokurssiOps[];
        pohjanTarjonta?: LukioOppiaine[];
    }

    export interface LukioOpetussuunnitelmaRakenneOps {
        perusteId: number;
        root: boolean;
        perusteen: LukioOpetussuunnitelmaRakennePeruste;
        oppiaineet: LukioOppiaine[];
        pohjanTarjonta: LukioOppiaine[];
    }
}