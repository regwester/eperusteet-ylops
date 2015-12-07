
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

    export interface Aihekokonaisuudet {
        uuidTunniste?: string;
        id? : number;
        otsikko?: l.Lokalisoitu;
        yleiskuvaus?: l.Lokalisoitu;
        aihekokonaisuudet: Aihekokonaisuus[];
    }

    export interface OpsAihekokonaisuus extends Aihekokonaisuus {
        perusteen?: Aihekokonaisuus;
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
        PAIKALLINEN_PAKOLLINEN,
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
        tyyppi: LukioKurssiTyyppiPeruste;
        tavoitteet?: l.TekstiOsa;
        keskeinenSisalto?: l.TekstiOsa;
        tavoitteetJaKeskeinenSisalto?: l.TekstiOsa;
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

    export interface LukioOppiaineTallennus extends Oppiaine {
        laajuus?: string;
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
        tila: Tila;
        oma: boolean;
        abstrakti?: boolean;
        jarjestys?: number;
        tyyppi: OppiaineTyyppi;
        laajuus: string;
        kurssiTyyppiKuvaukset: { [key:string/*LukioKurssiTyyppi, not supported in ts*/]: l.Lokalisoitu; };
        oppimaarat?: LukioOppiaine[];
        kurssit: LukiokurssiOps[];
    }

    export interface LukioOpetussuunnitelmaRakenneOps {
        perusteId: number;
        perusteen: LukioOpetussuunnitelmaRakennePeruste;
        oppiaineet: LukioOppiaine[];
    }
}