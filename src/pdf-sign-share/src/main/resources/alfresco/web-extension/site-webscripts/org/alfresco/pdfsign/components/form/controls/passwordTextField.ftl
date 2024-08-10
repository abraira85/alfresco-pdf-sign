<#-- Template for a password field -->

<div class="form-field" id="${field.name}-control">
    <label for="${fieldHtmlId}">${field.label?html}</label>
    <input id="${fieldHtmlId}" name="${field.name}" type="password" value="${field.value?html}" title="${field.label?html}" maxlength="255" tabindex="0">
</div>
