<#ftl encoding="utf-8"/>
<#-- @ftlvariable name="name" type="java.lang.String" -->
<#import "_macro/page.ftlh" as page>

<@page.defaultpage>
    <@page.script>
        111111111
    </@page.script>

    ${name} ${name?length}

    <@page.script>
        22222222222
    </@page.script>
</@page.defaultpage>