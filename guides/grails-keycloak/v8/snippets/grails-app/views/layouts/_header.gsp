<header class="site-header">
    <nav class="site-nav">
        <div class="nav-left">
            <g:link controller="home" class="nav-link">
                <g:message code='header.btn.home' />
            </g:link>
            <app:hasRole role="USER">
                <g:link controller="account" class="nav-link">
                    <g:message code='header.btn.account' />
                </g:link>
            </app:hasRole>
            <app:hasRole role="ADMIN">
                <g:link controller="admin" class="nav-link">
                    <g:message code='header.btn.admin' />
                </g:link>
            </app:hasRole>
        </div>
        <div class="nav-right">
            <app:loggedIn>
                <span class="user-name">
                    <app:currentUser>
                        ${it.firstName} ${it.lastName}
                    </app:currentUser>
                </span>
                <g:set var="csrfToken" value="${request.getAttribute('org.springframework.security.web.csrf.CsrfToken')}"/>
                <g:if test="${csrfToken instanceof java.util.function.Supplier}">
                    <g:set var="csrfToken" value="${csrfToken.get()}"/>
                </g:if>
                <form action="${createLink(uri: '/logout')}" method="post" class="logout-form">
                    <g:if test="${csrfToken}">
                        <g:hiddenField name="${csrfToken.parameterName}" value="${csrfToken.token}"/>
                    </g:if>
                    <button type="submit" class="logout-button">
                        <g:message code='header.btn.logout' />
                    </button>
                </form>
            </app:loggedIn>
             <app:ifNotLoggedIn>
                <g:link controller="account" class="login-button">
                    <g:message code='header.btn.login' />
                </g:link>
            </app:ifNotLoggedIn>
        </div>
    </nav>
</header>