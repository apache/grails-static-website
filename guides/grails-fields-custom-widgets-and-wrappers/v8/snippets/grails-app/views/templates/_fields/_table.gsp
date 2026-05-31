<table class="table table-striped table-hover align-middle">
    <thead class="table-dark">
        <tr>
            <g:each in="${domainProperties}" var="prop">
                <th scope="col">
                    <g:message code="${domainClass.javaClass.simpleName.toLowerCase()}.${prop.name}.label" default="${prop.naturalName}"/>
                </th>
            </g:each>
            <th scope="col" class="text-end">Actions</th>
        </tr>
    </thead>
    <tbody>
        <g:each in="${collection}" var="instance">
            <tr>
                <g:each in="${domainProperties}" var="prop">
                    <td>
                        <f:displayWidget bean="${instance}" property="${prop.name}"/>
                    </td>
                </g:each>
                <td class="text-end">
                    <g:link resource="${instance}" action="show" class="btn btn-sm btn-outline-secondary">View</g:link>
                    <g:link resource="${instance}" action="edit" class="btn btn-sm btn-outline-warning">Edit</g:link>
                    <g:form resource="${instance}" method="DELETE" class="d-inline">
                        <button type="submit" class="btn btn-sm btn-outline-danger"
                                onclick="return confirm('Delete &quot;' + '${instance.toString().encodeAsJavaScript()}' + '&quot;?');">
                            Delete
                        </button>
                    </g:form>
                </td>
            </tr>
        </g:each>
    </tbody>
</table>
