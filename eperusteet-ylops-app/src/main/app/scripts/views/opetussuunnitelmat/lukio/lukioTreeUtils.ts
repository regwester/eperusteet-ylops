/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */

enum LukioKurssiTreeNodeType {
    root,
    kurssi,
    oppiaine
}

ylopsApp.service("LukioTreeUtils", function($log, $state, $stateParams, Kaanna, Kieli, $timeout, $rootScope, $filter) {
    const transformKurssitToTreeNodes = ks => {
        const nodes = [];
        _.each(ks, (k: Lukio.LukiokurssiOps) => {
            const node = _.clone(k);
            node.dtype = LukioKurssiTreeNodeType.kurssi;
            node.lapset = [];
            node.$$tyyppi = node.tyyppi.toLowerCase();
            nodes.push(node);
        });
        return nodes;
    };

    const transformOppiaineToTreeNodes = oas => {
        var nodes = [];
        _.each(oas, (oa: Lukio.LukioOppiaine) => {
            var node = _.clone(oa);
            node.dtype = LukioKurssiTreeNodeType.oppiaine;
            node.lapset = _.union(
                transformOppiaineToTreeNodes(oa.oppimaarat || []),
                transformKurssitToTreeNodes(oa.kurssit || [])
            );
            _.each(node.lapset, lapsi => {
                lapsi.$$nodeParent = node;
            });
            nodes.push(node);
        });
        return nodes;
    };

    const treeRootLapsetFromRakenne = (rakenne: Lukio.LukioOpetussuunnitelmaRakenneOps) =>
        transformOppiaineToTreeNodes(rakenne.oppiaineet);

    const textMatch = (txt, to) => {
        if (!to) {
            return true;
        }

        if (!txt.length) {
            return false;
        }

        const words = to.toLowerCase().split(/\s+/);
        const found = {};

        for (const part in txt) {
            if (!txt[part]) {
                continue;
            }

            const lower = txt[part].toLowerCase();
            for (const i in words) {
                if (words[i] && lower.indexOf(words[i]) !== -1) {
                    found[i] = true;
                }
            }
        }

        for (let j = 0; j < words.length; ++j) {
            if (!found[j]) {
                return false;
            }
        }

        return true;
    };

    const matchesSearch = (node, search) => {
        if (!search) {
            return true;
        }

        let nimi = node.nimi;
        const koodiArvo = node.koodiArvo;
        const lokalisoituKoodi = node.lokalisoituKoodi ? Kaanna.kaanna(node.lokalisoituKoodi) : null;

        if (_.isObject(node.nimi)) {
            nimi = nimi[Kieli.getSisaltokieli().toLowerCase()];
        }

        return textMatch([nimi, koodiArvo, lokalisoituKoodi], search);
    };

    const search = (root, search) => {
        const ftree = _(root)
            .flattenTree(n => n.lapset)
            .filter(n => n.dtype != LukioKurssiTreeNodeType.root)
            .value();

        _.each(ftree, n => {
                n.$$hide = !matchesSearch(n, search);
                if (!n.$$hide) {
                    // Näytä myös vanhemmat
                    _.each(_.flattenTree(n, node => node.$$nodeParent), node => {
                        node.$$hide = false;
                    });
                }
            }
        );
    };

    const buildTree = rakenne => {
        const root = {
            dtype: LukioKurssiTreeNodeType.root,
            lapset: []
        };

        _.each(treeRootLapsetFromRakenne(rakenne), lapsi => {
            root.lapset.push(lapsi);
        });

        return root;
    };

    return {
        search: search,
        buildTree: buildTree
    };
});
