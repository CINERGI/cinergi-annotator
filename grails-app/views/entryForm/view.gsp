<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Cinergi Entry Form</title>
    <g:javascript library="jquery"/>
    <g:javascript>

        var deleteKeywordHandler = function (evt) {
            evt.preventDefault();
            var butId = $(this).attr('id')
            var delIdx = butId.lastIndexOf('_')
            var idx = butId.substr(delIdx + 1)
            console.log("idx:" + idx);
            $(this).parent().remove();
        };
        $('.kwd').on('click', deleteKeywordHandler);
        var newKwdIndex = 0;
        $('#kwAdd').on('click', function (evt) {
            evt.preventDefault();
            ++newKwdIndex;
            var kwAddDiv$ = $('#addKeywordTemplate').contents().clone()
            $('label', kwAddDiv$).each(function () {
                var oldFor = $(this).attr('for');
                $(this).attr("for", oldFor + "_n" + newKwdIndex);
            });
            $('input, select', kwAddDiv$).each(function () {
                var this$ = $(this);
                var oldName = this$.attr('name'), oldId = oldName;

                this$.attr('name', oldName + "_n" + newKwdIndex);
                this$.attr('id', oldId + "_n" + newKwdIndex);
            });
            $('.delete', kwAddDiv$).each(function () {
                $(this).attr('id', 'kwDelete_n' + newKwdIndex).addClass('kwd');
                $(this).on('click', deleteKeywordHandler)
            });
            kwAddDiv$.appendTo($('#keywordsPanel'));
        });
    </g:javascript>
    <style>
    fieldset {
        border: 1px solid seagreen;
        padding: 1em;
        margin: 1em;
    }

    legend {
        padding: 0.2em 0.5em;
        border: 1px solid seagreen;
    }

    label {
        width: 120px;
        margin-right: 0.5em;
        padding-top: 0.2em;
        text-align: right;
        font-weight: bold;
        font-size: 90%;
    }

    div.panel {
        padding-top: 1em;
    }

    input {
        margin-bottom: 0.2em;
    }

    h3 {
        padding: 1em;
    }
    </style>
</head>

<body>
<div class="nav" role="navigation">
    <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
        <g:if test="${session?.user}">
            <li><g:link class="list" controller="source" action="showSources">Sources</g:link></li>
            <li><g:link class="list" controller="User" action="logout">Logout</g:link></li>
        </g:if>
    </ul>
</div>

<div class="main">
    <g:form action="processEntry" name="entryForm">
        <div id="abstractPanel" class="panel">
            <fieldset>
                <legend>Title &amp; Description</legend>

                <div>
                    <label for="email">Email</label>
                    <input type="text" size="50" id="email" name="email" value="${email}">
                </div>

                <div>
                    <label for="title">Title</label>
                    <input type="text" size="50" id="title" name="title" value="${titleTxt}">
                </div>

                <div>
                    <label for="description">Description</label>
                    <textarea style="width: 75%;" id="description" name="description"
                              cols="100" rows="5">${abstractTxt}</textarea>
                </div>
            </fieldset>
        </div>

        <div id="keywords" class="panel">
            <fieldset>
                <legend>Keywords</legend>

                <div id="keywordsPanel">
                    <g:each in="${keywords}" status="i" var="kw">
                        <div>
                            <label for="keyword_${kw.id}">Keyword:</label>
                            <input id="keyword_${kw.id}" name="keyword_${kw.id}" type="text" value="${kw.keyword}"/>
                            <label for="category_${kw.id}">Category:</label>
                            <g:select name="category_${kw.id}" from="${[
                                    'Atmosphere', 'Chemical entity', 'Document',
                                    'Environmental material', 'Equipment', 'Geographic location',
                                    'Geosphere', 'Habitat', 'Human activity',
                                    'Location', 'Material entity', 'Observed Property',
                                    'Organization', 'Place', 'Process', 'Publication', 'Resource originator',
                                    'Specification', 'Theme', 'Unknown', 'Water body', 'chemical process',
                                    'dataCenter', 'dataResolution', 'dataset', 'discipline',
                                    'instrument', 'place', 'platform', 'project', 'stratum', 'temporal', 'theme']}"
                                      value="${kw.category}"/>
                            %{--  <input id="category_${kw.id}" type="text" value="${kw.category}" readonly/> --}%
                            <button type="button" class="delete kwd" id="kwDelete_${kw.id}">Delete</button>
                        </div>
                    </g:each>
                </div>

                <div class="buttons">
                    <button type="button" class="add" id="kwAdd">Add Keyword</button>
                </div>
            </fieldset>

            <div class="buttons">
                <button type="submit" class="edit" id="save">Submit</button>
            </div>
        </div>
    </g:form>
    <div id="addKeywordTemplate" style="display: none">
        <div>
            <label for="keyword">Keyword:</label>
            <input id="keyword" name="keyword" type="text" value=""/>
            <label>Category:</label>
            <g:select name="placeholder" from="${[
                    'Atmosphere', 'Chemical entity', 'Document',
                    'Environmental material', 'Equipment', 'Geographic location',
                    'Geosphere', 'Habitat', 'Human activity',
                    'Location', 'Material entity', 'Observed Property',
                    'Organization', 'Place', 'Process', 'Publication', 'Resource originator',
                    'Specification', 'Theme', 'Unknown', 'Water body', 'chemical process',
                    'dataCenter', 'dataResolution', 'dataset', 'discipline',
                    'instrument', 'place', 'platform', 'project', 'stratum', 'temporal', 'theme']}"/>

            <button type="button" class="delete">Delete</button>
        </div>
    </div>
</div>
</body>
</html>