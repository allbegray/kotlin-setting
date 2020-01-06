<#ftl encoding="utf-8"/>
<#-- @ftlvariable name="name" type="java.lang.String" -->
<#-- @ftlvariable name="r" type="hong.common.web.router.Router" -->
<#import "_macro/page.ftlh" as page>

<@page.defaultpage>
    <@page.script>
        <script>
            function a() {
            }
        </script>
    </@page.script>

    <div>
    ${name} ${name?length}
    </div>

    <div>
    ${r.mvcUrl("LoginAdminController#test").build()}

    </div>
    <div>
        ${r.mvcUrl("ArgController#pathVarialbeTest").arg(0, "hong").buildAndExpand("hahaha")}
    </div>
    <div>
        ${r.mvcUrl("ArgController#pathVarialbeTest22222222").buildAndExpand("hahaha")}
    </div>

    <@page.script>
        <script>
            function b() {
            }
        </script>
    </@page.script>
</@page.defaultpage>