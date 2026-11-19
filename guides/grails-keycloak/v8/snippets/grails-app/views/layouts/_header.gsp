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
                <g:form method="post" url="[uri: '/logout']" useToken="true" class="logout-form">
                    <button type="submit" class="logout-button">
                        <g:message code='header.btn.logout' />
                    </button>
                </g:form>
            </app:loggedIn>
             <app:ifNotLoggedIn>
                <g:link controller="account" class="login-button">
                    <g:message code='header.btn.login' />
                </g:link>
            </app:ifNotLoggedIn>
        </div>
    </nav>
</header>