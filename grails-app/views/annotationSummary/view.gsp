<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Annotation Summary</title>
    <g:javascript library="jquery"/>
    <style>
    div.paginateButtons {
        font-size: 1.3em;
        line-height: 150%;
        padding: 5px;
    }

    .paginateButtons a.step, a.nextLink, a.prevLink, span.currentStep {
        padding-left: 5px;
    }

    </style>
</head>

<body>
<div class="nav" role="navigation">
    <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
        <g:if test="${session?.user}">
        %{--
        <li><g:link class="list" controller="EntryForm" action="index">Entry Form</g:link></li>
        --}%
            <li><g:link class="list" controller="User" action="logout"><g:message code="Logout"
                                                                                  args="[entityName]"/></g:link></li>
        </g:if>
    </ul>
</div>

<div class="main">
    <div style="margin:5px;">
        <g:form action="show">

        </g:form>
        <table style="width: 100%; table-layout: fixed;">
            <thead>
            <th>Source</th>
            <th>Doc ID</th>


            </thead>
            <tbody>
            <g:each in="${dwList}" status="i" var="dw">
                <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                    <td>${dw.SourceInfo.Name}</td>
                    <td>
                        ${dw.primaryKey}
                    </td>
                </tr>
                <tr>
                    <td colspan="2">
                        <table style="width: 100%; table-layout: fixed;">
                            <thead>
                            <th>Action</th>
                            <th>Old Term</th>
                            <th>Old Category</th>
                            <th>New Term</th>
                            <th>New Category</th>
                            </thead>
                            <g:each in="${dw.Data.annotatedKeywords}" status="j" var="ak">
                                <tr>
                                    <td>${ak.annotationAction}</td>
                                    <td>${ak.oldTerm}</td>
                                    <td>${ak.oldCategory}</td>
                                    <td>${ak.newTerm}</td>
                                    <td>${ak.newCategory}</td>
                                </tr>
                            </g:each>
                        </table>
                    </td>
                </tr>
            </g:each>
            </tbody>
         <tfoot>
        <div class="paginateButtons">
            <g:paginate total="${totCount}"
                        params="${[totCount: totCount]}"/>
        </div>
        </tfoot>
        </table>
    </div>

</div>
</body>
</html>