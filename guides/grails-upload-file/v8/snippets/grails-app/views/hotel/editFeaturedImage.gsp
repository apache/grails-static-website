<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main" />
    <g:set var="entityName" value="${message(code: 'hotel.label', default: 'Hotel')}" />
    <title><g:message code="default.edit.label" args="[entityName]" /></title>
</head>
<body>
<div id="content" role="main">
    <div class="container">
        <section class="row">
            <a href="#edit-hotel" class="visually-hidden-focusable" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
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
            <div id="edit-hotel" class="col-12 content scaffold-edit" role="main">
                <h1><g:message code="default.edit.label" args="[entityName]" /></h1>
                <g:flashMessages />
                <g:hasErrors bean="${this.hotel}">
                    <ul class="alert alert-danger list-unstyled" role="alert">
                        <g:eachError bean="${this.hotel}" var="error">
                            <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><i class="bi-exclamation-circle"></i> <g:message error="${error}"/></li>
                        </g:eachError>
                    </ul>
                </g:hasErrors>
                %{-- tag::uploadForm[] --}%
                <g:uploadForm name="uploadFeaturedImage" action="uploadFeaturedImage">
                    <g:hiddenField name="id" value="${this.hotel?.id}" />
                    <g:hiddenField name="version" value="${this.hotel?.version}" />
                    <div class="mb-3">
                        <label for="featuredImageFile" class="form-label"><g:message code="hotel.featuredImageFile.label" default="Featured Image (JPG or PNG, up to 25MB)" /></label>
                        <input type="file" id="featuredImageFile" name="featuredImageFile" class="form-control" accept="image/jpeg,image/png" />
                    </div>
                    <fieldset class="bg-body-tertiary">
                        <button class="btn btn-outline-primary" type="submit">
                            <i class="bi-upload"></i> ${message(code: 'hotel.featuredImage.upload.label', default: 'Upload')}
                        </button>
                    </fieldset>
                </g:uploadForm>
                %{-- end::uploadForm[] --}%
            </div>
        </section>
    </div>
</div>
</body>
</html>
