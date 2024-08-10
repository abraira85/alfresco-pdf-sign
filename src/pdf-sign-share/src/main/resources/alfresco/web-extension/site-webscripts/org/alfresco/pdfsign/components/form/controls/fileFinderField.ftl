<#-- Template for a File Finder type field -->

<div class="form-field" id="${fieldHtmlId}-control" style="display: none;">
    <label for="${fieldHtmlId}-cntrl">${field.label?html}</label>

    <div id="${fieldHtmlId}-cntrl" class="object-finder">

        <div id="${fieldHtmlId}-cntrl-currentValueDisplay" class="current-values object-finder-items"></div>

        <input type="hidden" id="${fieldHtmlId}" name="-" value="${field.value?html}">
        <input type="hidden" id="${fieldHtmlId}-cntrl-added" name="${field.name}_added">
        <input type="hidden" id="${fieldHtmlId}-cntrl-removed" name="${field.name}_removed">

        <div id="${fieldHtmlId}-cntrl-itemGroupActions" class="show-picker">
            <span class="yui-button yui-push-button" id="${fieldHtmlId}-button">
                <span class="first-child">
                    <button type="button" tabindex="0" id="${fieldHtmlId}-button-action">${msg("button.select")}</button>
                </span>
            </span>
        </div>

    </div>
</div>
