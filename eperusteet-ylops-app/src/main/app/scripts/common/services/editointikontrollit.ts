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

/**
 * Editointikontrollit
 * required callbacks:
 * - edit Called when starting to edit
 * - save Called when saving
 * - cancel Called when canceling edit mode
 * optional callbacks:
 * - notify Called when edit mode changes with boolean parameter editMode
 */
ylopsApp.factory("Editointikontrollit", function($rootScope, $q, $timeout, $log, Utils, Notifikaatiot) {
    var scope = $rootScope.$new(true);
    scope.editingCallback = null;
    scope.editMode = false;

    scope.editModeDefer = $q.defer();

    this.lastModified = null;
    var cbListener = _.noop;
    var editmodeListener = null;

    function setEditMode(mode) {
        scope.editMode = mode;
        scope.editModeDefer = $q.defer();
        scope.editModeDefer.resolve(scope.editMode);
        if (scope.editingCallback) {
            scope.editingCallback.notify(mode);
        }
        if (editmodeListener) {
            editmodeListener(mode);
        }
    }

    function handleBadCode(maybe, resolve = _.noop, reject = _.noop, xfinally = _.noop) {
        if (!scope.editingCallback) {
            $log.error("Editing object missing");
            return;
        }

        if (_.isObject(maybe) && _.isFunction(maybe.then)) {
            var x = maybe.then(resolve);
            if (x && x.catch) {
                x.catch(reject).finally(xfinally);
            }
        } else {
            $log.error("You should be using a promise");
            resolve();
        }
    }

    return {
        startEditing: () => {
            var deferred = $q.defer();
            handleBadCode(scope.editingCallback.edit(), function() {
                setEditMode(true);
                $rootScope.$broadcast("enableEditing");
                deferred.resolve();
            });
            return deferred.promise;
        },
        saveEditing: function(kommentti) {
            $rootScope.$broadcast("editointikontrollit:preSave");
            $rootScope.$broadcast("notifyCKEditor");
            var err;

            function mandatoryFieldValidator(fields, target) {
                err = undefined;
                var fieldsf = _.filter(fields || [], function(field) {
                    return field.mandatory;
                });

                if (!target) {
                    return false;
                } else if (_.isString(target)) {
                    return !_.isEmpty(target);
                } else if (_.isObject(target) && !_.isEmpty(target) && !_.isEmpty(fieldsf)) {
                    return _.all(fieldsf, function(field) {
                        var valid = Utils.hasLocalizedText(target[field.path]);
                        if (!valid) {
                            err = field.mandatoryMessage;
                        }
                        return valid;
                    });
                } else {
                    return true;
                }
            }

            function afterSave() {
                setEditMode(false);
                $rootScope.$broadcast("disableEditing");
            }

            function after() {
                if (scope.editingCallback.validate(mandatoryFieldValidator)) {
                    handleBadCode(scope.editingCallback.save(kommentti), afterSave);
                } else {
                    if (!scope.editingCallback.doNotShowMandatoryMessage) {
                        Notifikaatiot.varoitus(err || "mandatory-odottamaton-virhe");
                    }
                }
            }

            if (scope.editingCallback) {
                if (_.isFunction(scope.editingCallback.asyncValidate)) {
                    scope.editingCallback.asyncValidate(after);
                } else {
                    after();
                }
            }
        },
        cancelEditing: function(tilanvaihto) {
            if (!_.isEmpty(scope.editingCallback)) {
                handleBadCode(scope.editingCallback.cancel(), () => {
                    setEditMode(false);
                    $rootScope.$broadcast("disableEditing");
                    $rootScope.$broadcast("notifyCKEditor");
                });
            }
        },
        registerCallback: function(callback) {
            callback.validate = callback.validate || _.constant(true);
            callback.edit = callback.edit || _.noop;

            if (
                !callback ||
                !_.isFunction(callback.edit) ||
                !_.isFunction(callback.save) ||
                !_.isFunction(callback.cancel)
            ) {
                console.error("callback-function invalid");
                throw "editCallback-function invalid";
            }

            if (!_.isFunction(callback.notify)) {
                callback.notify = _.noop;
            }

            editmodeListener = null;
            scope.editingCallback = callback;
            scope.editModeDefer.resolve(scope.editMode);
            cbListener();
        },
        unregisterCallback: function() {
            setEditMode(false);
            scope.editingCallback = null;
        },
        editingEnabled: () => {
            return !!scope.editingCallback;
        },
        registerCallbackListener: function(callbackListener) {
            cbListener = callbackListener;
        },
        registerEditModeListener: function(listener) {
            editmodeListener = listener;
        },
        getEditModePromise: function() {
            return scope.editModeDefer.promise;
        },
        getEditMode: function() {
            return scope.editMode;
        }
    };
});
