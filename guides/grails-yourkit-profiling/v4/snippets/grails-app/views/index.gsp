<!doctype html>
<html>
<head>
    <title>Grails Yourkit Profile</title>
    <meta name="layout" content="main"/>
</head>
<body>
<content tag="nav">
    <li class="dropdown">
        <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">Application Status <span class="caret"></span></a>
        <ul class="dropdown-menu">
            <li><a href="#">Environment: ${grails.util.Environment.current.name}</a></li>
            <li><a href="#">App profile: ${grailsApplication.config.grails?.profile}</a></li>
            <li><a href="#">App version:
                <g:meta name="info.app.version"/></a>
            </li>
            <li role="separator" class="divider"></li>
            <li><a href="#">Grails version:
                <g:meta name="info.app.grailsVersion"/></a>
            </li>
            <li><a href="#">Groovy version: ${GroovySystem.getVersion()}</a></li>
            <li><a href="#">JVM version: ${System.getProperty('java.version')}</a></li>
            <li role="separator" class="divider"></li>
            <li><a href="#">Reloading active: ${grails.util.Environment.reloadingAgentEnabled}</a></li>
        </ul>
    </li>
    <li class="dropdown">
        <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">Artefacts <span class="caret"></span></a>
        <ul class="dropdown-menu">
            <li><a href="#">Controllers: ${grailsApplication.controllerClasses.size()}</a></li>
            <li><a href="#">Domains: ${grailsApplication.domainClasses.size()}</a></li>
            <li><a href="#">Services: ${grailsApplication.serviceClasses.size()}</a></li>
            <li><a href="#">Tag Libraries: ${grailsApplication.tagLibClasses.size()}</a></li>
        </ul>
    </li>
    <li class="dropdown">
        <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">Installed Plugins <span class="caret"></span></a>
        <ul class="dropdown-menu">
            <g:each var="plugin" in="${applicationContext.getBean('pluginManager').allPlugins}">
                <li><a href="#">${plugin.name} - ${plugin.version}</a></li>
            </g:each>
        </ul>
    </li>
</content>

    <div id="content" role="main">
        <section class="row colset-2-its">
            <h1>Grails Yourkit Profile</h1>

            <div id="controllers" role="navigation">
                <h2>Available Controllers:</h2>
                <ul>
                    <g:each var="c" in="${grailsApplication.controllerClasses.sort { it.fullName } }">
                        <li class="controller">
                            <g:link controller="${c.logicalPropertyName}">${c.name}</g:link>
                        </li>
                    </g:each>
                </ul>

                <h2>Grails YourKit Profiling Operations</h2>
                <table id="profileOperations">
                    <tr>
                        <td><a href="${createLink (controller:"student", action: "insert")}">Insert Students</a>
                     </tr>
                    <tr>
                        <td><a href="${createLink (controller:"student", action: "delete")}">Delete Students</a>
                    </tr>
                    <tr>
                        <td><a href="${createLink (controller:"student", action: "print")}">Print Students</a>
                    </tr>
                    <tr>
                        <td><a href="${createLink (controller:"student", action: "import25kStudents")}">Import 25k Students</a>
                    </tr>
                    <tr>
                        <td><a href="${createLink (controller:"student", action: "import75kStudents")}">Import 75k Students</a>
                    </tr>
                </table>
            </div>
        </section>
    </div>

</body>
</html>
