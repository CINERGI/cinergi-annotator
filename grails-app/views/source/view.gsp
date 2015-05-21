<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Cinergi Sources</title>
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
    <g:javascript>
        $(function () {
            $('#selectedSource').on('change', this, function (evt) {
                var selSourceId = $(':selected', this).val();
                $('#selectedSourceId').val(selSourceId);
                $(this).closest('form').trigger('submit');
            });
        });
    </g:javascript>
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
        <g:form action="showSources">
            <input type="hidden" id="selectedSourceId" name="selectedSourceId" value="${selectedSource}"/>
            <g:select id="selectedSource" name="selectedSource" from="${siList}"
                      optionKey="resourceId" optionValue="name" value="${selectedSource}"/>
        </g:form>
    </div>
    <table style="width: 100%; table-layout: fixed;">
        <thead>
        <th>Doc ID</th>
        <th style="width: 85px;">Annotation</th>
        </thead>
        <tbody>
        <g:each in="${dwList}" status="i" var="dw">
            <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                <td>
                    ${dw.primaryKey}
                </td>
                <td style="width:85px;">
                    <g:link controller="Annotation" action="index" params="${[docId: dw.primaryKey]}">Annotate</g:link>
                </td>
            </tr>
        </g:each>
        </tbody>
        <tfoot>
        <div class="paginateButtons">
            <g:paginate total="${totCount}"
                        params="${[totCount: totCount, selectedSource:selectedSource]}"/>
        </div>
        </tfoot>
    </table>
</div>
</body>
</html>