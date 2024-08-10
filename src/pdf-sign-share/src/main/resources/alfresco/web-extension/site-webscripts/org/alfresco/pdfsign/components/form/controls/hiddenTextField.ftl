<#-- Template para un campo de texto oculto inicialmente -->

<div class="form-field" id="${field.name}-control" style="display: none;">
    <label for="${fieldHtmlId}">${field.label?html}</label>
    <input id="${fieldHtmlId}" name="${field.name}" type="text" value="${field.value?html}" title="${field.label?html}" maxlength="255" tabindex="0">
</div>
