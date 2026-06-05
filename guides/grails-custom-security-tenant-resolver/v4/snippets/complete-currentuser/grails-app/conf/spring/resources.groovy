//tag::currentUserTenantResolverImport[]
import demo.CurrentUserTenantResolver
//end::currentUserTenantResolverImport[]
//tag::passwordEncodingImport[]
import demo.UserPasswordEncoderListener
//end::passwordEncodingImport[]
// Place your Spring DSL code here
//tag::beans[]
beans = {
//end::beans[]
//tag::passwordEncodingBean[]
    userPasswordEncoderListener(UserPasswordEncoderListener)
//end::passwordEncodingBean[]
//tag::currentUserTenantResolver[]
    currentUserTenantResolver(CurrentUserTenantResolver)
//end::currentUserTenantResolver[]
}
