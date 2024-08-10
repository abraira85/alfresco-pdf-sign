/**
 * PDF Sign page selection component.
 *
 * @namespace PDFSign
 * @class PDFSign.SelectPage
 */
if (typeof PDFSign == "undefined" || !PDFSign) {
    var PDFSign = {};
}

PDFSign.Util = {};

(function() {
    /**
     * YUI Library aliases
     */
    var Dom = YAHOO.util.Dom,
        Event = YAHOO.util.Event,
        Selector = YAHOO.util.Selector;

    /**
     * SelectPage constructor.
     *
     * @param {String} htmlId The HTML id of the parent element
     * @return {PDFSign.SelectPage} The new component instance
     * @constructor
     */
    PDFSign.SelectPage = function SelectPage_constructor(htmlId) {
        PDFSign.SelectPage.superclass.constructor.call(this, "PDFSign.SelectPage", htmlId, []);
        return this;
    };

    YAHOO.extend(PDFSign.SelectPage, Alfresco.component.Base, {
        /**
         * Object container for initialization options
         *
         * @property options
         * @type {object} object literal
         */
        options: {
            /**
             * Reference to the PDF document
             *
             * @property nodeRef
             * @type {string}
             */
            nodeRef: null,

            /**
             * Do we show the page scheme options?
             *
             * @property showPageScheme
             * @type {boolean}
             */
            showPageScheme: false,

            /**
             * Do we permit multiple pages to be selected?
             *
             * @property allowMultiPageSelect
             * @type {boolean}
             */
            allowMultiPageSelect: false
        },

        schemesModule: null,
        pagesModule: null,
        pageCount: -1,

        /**
         * Initializes the component when ready.
         *
         * @method onReady
         */
        onReady: function SelectPage_onReady() {
            this.getPageCount(this.options.nodeRef);
            this.pagesModule = new YAHOO.widget.Module(this.id + "-pageModule");

            if (this.options.showPageScheme === "true") {
                this.schemesModule = new YAHOO.widget.Module(this.id + "-schemeModule");
                this.getPageSchemes(this.options.nodeRef);
                // default state is page select hidden if page schemes enabled
                this.pagesModule.hide();
            }

            this.setValue(null, this);

            YAHOO.util.Event.addListener([this.id + "-useScheme"], "click", this.toggleSchemes, this);
        },

        /**
         * Fetches the page count for the given PDF document.
         *
         * @param {string} nodeRef The node reference of the PDF document
         * @method getPageCount
         */
        getPageCount: function SelectPage_getPageCount(nodeRef) {
            Alfresco.util.Ajax.jsonGet({
                url: (Alfresco.constants.PROXY_URI + "pdfsign/pagecount?nodeRef=" + nodeRef),
                successCallback: {
                    fn: function(response) {
                        var pageSelect = YAHOO.util.Dom.get(this.id + "-pages");
                        YAHOO.util.Event.addListener(pageSelect, "change", this.setValue, this);

                        var pages = parseInt(response.json.pageCount);
                        if (pages > 0) {
                            // Add "All Pages" option
                            var optAll = document.createElement("option");
                            optAll.text = "All Pages";
                            optAll.value = "all";
                            pageSelect.add(optAll, null);

                            // Add individual page options
                            for (var i = 1; i <= pages; i++) {
                                var opt = document.createElement("option");
                                opt.text = i;
                                opt.value = i;
                                pageSelect.add(opt, null);
                            }

                            // Set the last page as selected by default
                            pageSelect.value = pages;
                            this.setValue(null, this);
                        }
                    },
                    scope: this
                },
                failureCallback: {
                    fn: function(response) {
                        Alfresco.util.PopupManager.displayMessage({
                            text: "Could not retrieve page count"
                        });
                    }
                }
            });
        },

        /**
         * Fetches the page schemes for the given PDF document.
         *
         * @param {string} nodeRef The node reference of the PDF document
         * @method getPageSchemes
         */
        getPageSchemes: function SelectPage_getPageSchemes(nodeRef) {
            Alfresco.util.Ajax.jsonGet({
                url: (Alfresco.constants.PROXY_URI + "pdfsign/pageschemes?nodeRef=" + nodeRef),
                successCallback: {
                    fn: function(response) {
                        var schemeSelect = YAHOO.util.Dom.get(this.id + "-schemes");
                        YAHOO.util.Event.addListener(schemeSelect, "change", this.setValue, this);

                        var schemes = response.json.schemes;
                        for (index in schemes) {
                            var opt = document.createElement("option");
                            opt.text = schemes[index].name;
                            opt.value = schemes[index].value;
                            schemeSelect.add(opt, null);
                        }
                        this.setValue(null, this);
                    },
                    scope: this
                },
                failureCallback: {
                    fn: function(response) {
                        Alfresco.util.PopupManager.displayMessage({
                            text: "Could not retrieve page schemes"
                        });
                    }
                }
            });
        },

        /**
         * Toggles the visibility of page schemes and page numbers.
         *
         * @param {Event} event The event object
         * @param {PDFSign.SelectPage} that The component instance
         * @method toggleSchemes
         */
        toggleSchemes: function SelectPage_toggleSchemes(event, that) {
            if (event.target.checked) {
                that.schemesModule.show();
                that.pagesModule.hide();
                that.setValue(event, that);
            } else {
                that.schemesModule.hide();
                that.pagesModule.show();
                that.setValue(event, that);
            }
        },

        /**
         * Sets the value of the hidden field based on the selected page or scheme.
         *
         * @param {Event} event The event object
         * @param {PDFSign.SelectPage} that The component instance
         * @method setValue
         */
        setValue: function SelectPage_setValues(event, that) {
            var useScheme = false;
            if (YAHOO.util.Dom.get(that.id + "-useScheme")) {
                useScheme = YAHOO.util.Dom.get(that.id + "-useScheme").checked;
            }
            var schemeSelect = YAHOO.util.Dom.get(that.id + "-schemes");
            var pageSelect = YAHOO.util.Dom.get(that.id + "-pages");
            var hiddenValue = YAHOO.util.Dom.get(that.id);

            if (useScheme) {
                if (schemeSelect.selectedIndex != -1) {
                    hiddenValue.value = schemeSelect.options[schemeSelect.selectedIndex].value;
                }
            } else {
                if (pageSelect.selectedIndex != -1) {
                    if (pageSelect.value === "all") {
                        hiddenValue.value = "all";  // Represent all pages
                    } else {
                        var selected = [];
                        var options = pageSelect.options;
                        for (var i = 0; i < options.length; i++) {
                            if (options[i].selected) {
                                selected.push(options[i].value);
                            }
                        }
                        hiddenValue.value = selected.join(",");
                    }
                }
            }
        }
    });
})();

(function() {
    /**
     * YUI Library aliases
     */
    var Dom = YAHOO.util.Dom,
        Event = YAHOO.util.Event,
        Selector = YAHOO.util.Selector;

    /**
     * DependentSelect constructor.
     *
     * @param {String} htmlId The HTML id of the parent element
     * @return {PDFSign.DependentSelect} The new component instance
     * @constructor
     */
    PDFSign.DependentSelect = function DependentSelect_constructor(htmlId) {
        PDFSign.DependentSelect.superclass.constructor.call(this, "PDFSign.DependentSelect", htmlId, []);
        return this;
    };

    YAHOO.extend(PDFSign.DependentSelect, Alfresco.component.Base, {
        /**
         * Object container for initialization options
         *
         * @property options
         * @type {object} object literal
         */
        options: {
            /**
             * The show / hide configuration(s) for the form controls
             *
             * @property showSelectValues
             * @type {Array} Array of objects with show/hide configurations
             */
            showSelectValues: []
        },

        /**
         * Initializes the component when ready.
         *
         * @method onReady
         */
        onReady: function DependentSelect_onReady() {
            YAHOO.util.Event.addListener([this.id], "change", this.onChange, this);
            var select = YAHOO.util.Dom.get(this.id);
            this.toggleDependentFields(this.options.showSelectValues, select, this.options.htmlId);
        },

        /**
         * Handles the change event of the select element.
         *
         * @param {Event} event The event object
         * @param {PDFSign.DependentSelect} that The component instance
         * @method onChange
         */
        onChange: function DependentSelect_onChange(event, that) {
            var config = that.options.showSelectValues;
            var htmlId = that.options.htmlId;
            var select = event.srcElement;

            that.toggleDependentFields(config, select, htmlId);
        },

        /**
         * Toggles the visibility of dependent fields based on the selected value.
         *
         * @param {Array} config The configuration for showing/hiding fields
         * @param {HTMLSelectElement} select The select element
         * @param {string} htmlId The HTML id prefix for dependent fields
         * @method toggleDependentFields
         */
        toggleDependentFields: function DependentSelect_toggleDependentFields(config, select, htmlId) {
            for (index in config) {
                var name = config[index].name;
                var fields = config[index].fields;

                if (name === select.value) {
                    for (fieldIndex in fields) {
                        var field = YAHOO.util.Dom.get(htmlId + "_" + fields[fieldIndex]);
                        var container = field.parentElement;
                        container.style.display = 'block';
                    }
                } else {
                    for (fieldIndex in fields) {
                        var field = YAHOO.util.Dom.get(htmlId + "_" + fields[fieldIndex]);
                        var container = field.parentElement;
                        container.style.display = 'none';
                    }
                }
            }
        }
    });
})();

(function() {
    /**
     * YUI Library aliases
     */
    var Dom = YAHOO.util.Dom,
        Event = YAHOO.util.Event,
        Selector = YAHOO.util.Selector;

    /**
     * Shows dependent controls based on the state of a checkbox.
     *
     * @param {String} fieldHtmlId The HTML id of the checkbox
     * @param {String} htmlIdPrefix The HTML id prefix for dependent fields
     * @method ShowDependentControls
     */
    PDFSign.Util.ShowDependentControls = function(fieldHtmlId, htmlIdPrefix) {
        var value = YAHOO.util.Dom.get(fieldHtmlId + "-entry").checked;
        YAHOO.util.Dom.get(fieldHtmlId).value = value;
        var controls = YAHOO.util.Dom.get(fieldHtmlId + "-tohide").value.split(",");

        for (var index in controls) {
            var control = YAHOO.util.Dom.get((htmlIdPrefix + "_" + controls[index]));
            var container = control.parentElement;
            if (value) {
                container.style.display = 'none';
            } else {
                container.style.display = 'block';
            }
        }
    }
})();
