<#ftl encoding="utf-8"/>
<#import "/spring.ftl" as spring/>

<#global scripts=''>

<#macro defaultpage title='default title...'>
    <!DOCTYPE html>
    <html lang="en">
    <@head title=title />

    <body>
    <#nested/>
    <@js/>
    ${scripts}
    </body>
    </html>
</#macro>

<#macro head title>
    <head>
        <title>${title}</title>
        <meta charset="utf-8"/>
        <@css/>
    </head>
</#macro>

<#macro css>
</#macro>

<#macro js>
</#macro>

<#macro script>
    <#global scripts>${scripts}<#nested/></#global>
</#macro>