<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'restaurant.label', default: 'Restaurant')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <a href="#show-restaurant" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
        <div class="nav" role="navigation">
            <ul>
                <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
                <li><g:link class="list" action="index"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
                <li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
            </ul>
        </div>
        <div id="show-restaurant" class="content scaffold-show" role="main">
            <h1><g:message code="default.show.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message" role="status">${flash.message}</div>
            </g:if>
            <h1><f:display bean="restaurant" property="name" /></h1>
            <g:if test="${this.restaurant.featuredImageBytes}">
                <img src="<g:createLink controller="restaurant" action="featuredImage" id="${this.restaurant.id}"/>" width="400"/>
            </g:if>
            <g:form resource="${this.restaurant}" method="DELETE">
                <fieldset class="buttons">
                    <g:link class="edit" action="editFeaturedImage" resource="${this.restaurant}"><g:message code="restaurant.featuredImageUrl.edit.label" default="Edit Featured Image" /></g:link>
                    <g:link class="edit" action="edit" resource="${this.restaurant}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
                    <input class="delete" type="submit" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
                </fieldset>
            </g:form>
        </div>
    </body>
</html>
