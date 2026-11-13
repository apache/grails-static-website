<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main" />
    <g:set var="entityName" value="${message(code: 'restaurant.label', default: 'Restaurant')}" />
    <title><g:message code="default.show.label" args="[entityName]" /></title>
</head>
<body>
<div id="content" role="main">
    <div class="container">
        <section class="row">
            <a href="#show-restaurant" class="visually-hidden-focusable" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
            <nav class="navbar navbar-expand-lg bg-body-tertiary">
                <ul class="navbar-nav container-fluid">
                    <li class="nav-item"><a class="nav-link btn" aria-label="Home" href="${createLink(uri: '/')}">
                        <i class="bi-house"></i> <g:message code="default.home.label"/></a>
                    </li>
                    <li class="nav-item"><g:link class="nav-link btn" aria-label="List" action="index">
                        <i class="bi-database"></i> <g:message code="default.list.label" args="[entityName]" /></g:link>
                    </li>
                    <li class="nav-item me-lg-auto">
                        <g:link class="nav-link btn" aria-label="New" action="create"><i class="bi-database-add"></i> <g:message code="default.new.label" args="[entityName]" /></g:link>
                    </li>
                </ul>
            </nav>
        </section>
        <section class="row">
            <div id="show-restaurant" class="col-12 content scaffold-show" role="main">
                <h1><g:message code="default.show.label" args="[entityName]" /></h1>
                <g:flashMessages />
                <h2><f:display bean="restaurant" property="name" /></h2>
                %{-- tag::featuredImage[] --}%
                <g:if test="${this.restaurant.featuredImageBytes}">
                    <img src="${createLink(controller: 'restaurant', action: 'featuredImage', id: this.restaurant.id)}" class="img-fluid" width="400" alt="${this.restaurant.name}"/>
                </g:if>
                %{-- end::featuredImage[] --}%
                <g:form resource="${this.restaurant}" method="DELETE">
                    <fieldset class="bg-body-tertiary">
                        <g:link class="btn btn-outline-primary" action="editFeaturedImage" resource="${this.restaurant}">
                            <i class="bi-image"></i> <g:message code="restaurant.featuredImage.edit.label" default="Edit Featured Image" />
                        </g:link>
                        <g:link class="btn btn-outline-primary" action="edit" resource="${this.restaurant}">
                            <i class="bi-pencil-square"></i> <g:message code="default.button.edit.label" default="Edit" />
                        </g:link>
                        <button class="btn btn-outline-primary" type="submit" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                            <i class="bi-trash"></i> ${message(code: 'default.button.delete.label', default: 'Delete')}
                        </button>
                    </fieldset>
                </g:form>
            </div>
        </section>
    </div>
</div>
</body>
</html>
