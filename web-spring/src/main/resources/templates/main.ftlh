<#ftl encoding="utf-8"/>
<#-- @ftlvariable name="name" type="java.lang.String" -->
<#-- @ftlvariable name="r" type="hong.common.web.router.ReverseRouter" -->
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
        ${r.currentUrlFor()}<br>
        ${r.currentUrlFor().withQueryString()}<br>
        ${r.currentUrlFor().withQueryString().queryParam("addParam", 1).queryParam("addParam2", "가", "나")}<br>
        ${r.currentUrlFor().withQueryString().replaceQueryParam("addParam", 123123).queryParam("addParam2", "가", "나")}<br>
        ${r.currentUrlFor().withQueryString().replaceQueryParam("addParam", 123123).queryParam("addParam", 456).queryParam("addParam2", "가", "나")}<br>
    </div>

    <div>
    ${r.urlFor("loginAdminController.test")}

    </div>
    <div>
        ${r.urlFor("argController.pathVarialbeTest").arg("hong1", "zzz").arg("userId", "ggg")}
    </div>
    <div>
        ${r.urlFor("argController.pathVarialbeTest22222222").arg("hong1", "aaaa")}
    </div>

    <@page.script>
        <script>
            function b() {
            }
        </script>
    </@page.script>
</@page.defaultpage>