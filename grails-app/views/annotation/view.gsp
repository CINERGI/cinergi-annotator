<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Cinergi Annotator</title>

    <g:javascript library="jquery"/>
    <script type="text/javascript" src="https://maps.google.com/maps/api/js"></script>
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
    <g:javascript>
        $(function () {
            function initializeMap(sl, wl, nl, el, mapCanvasId) {

                var bounds = new google.maps.LatLngBounds(
                        new google.maps.LatLng(sl, wl),
                        new google.maps.LatLng(nl, el)
                );
                console.log(bounds.toString())
                var mapOptions = {
                    //center: bounds.getCenter(),
                    center: new google.maps.LatLng(sl, wl),
                    //  center: {
                    //      lat: sl, lng: wl
                    //  },
                    zoom: 8
                };
                var map = new google.maps.Map(document.getElementById(mapCanvasId),
                        mapOptions);
                var brOpts = {
                    bounds: bounds, map: map,
                    editable: true, draggable: true
                };
                var br = new google.maps.Rectangle(brOpts);

                //map.fitBounds(bounds);
                var delIdx = mapCanvasId.lastIndexOf('_')
                var bbId = mapCanvasId.substr(delIdx + 1)

                function handleBBEditing() {
                    google.maps.event.addListener(br, 'bounds_changed', function (evt) {
                        var ne = br.getBounds().getNorthEast();
                        var sw = br.getBounds().getSouthWest();
                        $('#sl_' + bbId).val(sw.lat());
                        $('#wl_' + bbId).val(sw.lng());
                        $('#nl_' + bbId).val(ne.lat());
                        $('#el_' + bbId).val(ne.lng());
                    });
                }

                handleBBEditing();
            }

            $('.bb').each(function (idx) {
                var id = idx + 1, sl, wl, nl, el, this$ = $(this);
                var slEl$ = $('#sl_' + id, this$);
                if (slEl$.length == 1) {
                    sl = $.trim(slEl$.val());
                    wl = $.trim($('#wl_' + id, this$).val());
                    nl = $.trim($('#nl_' + id, this$).val());
                    el = $.trim($('#el_' + id, this$).val());
                    initializeMap(sl, wl, nl, el, 'map-canvas_' + id);
                }
            });

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
                var kwAddDiv$ = $('#addKeywordTemplate').contents().clone();
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

            $('.bbDel').on('click', function (evt) {
                evt.preventDefault();
                $(this).closest('.bb').remove();
                var form$ = $('#annotationForm')
                form$.submit();
            });

           // $('#kwSave').prop('title','Not updated for new Keyword Enhancer changes yet!').prop('disabled',true);
        });
    </g:javascript>
</head>

<body>
<div class="nav" role="navigation">
    <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
        <g:if test="${session?.user}">
            <li><g:link class="list" controller="source" action="showSources"
                        params="${[selectedSource: sourceID, enhancedOnly: enhancedOnly]}">Sources</g:link></li>
            <li><g:link class="list" controller="User" action="logout">Logout</g:link></li>
        </g:if>
    </ul>
</div>

<div class="main">
    <div id="abstractPanel" class="panel">
        <h3>Doc ID:${docId} (${sourceName})</h3>
        <fieldset>
            <legend>Abstract</legend>
            <g:if test="${titleTxt}">
                <h4>${titleTxt}</h4>
            </g:if>
            <textarea style="width: 75%;" cols="100" rows="5" readonly>${abstractTxt}</textarea>
        </fieldset>
    </div>
    <g:form action="saveAnnotations" name="annotationForm">
        <input type="hidden" name="docId" value="${docId}"/>

        <div id="keywords" class="panel">
            <fieldset>
                <legend>Enhanced Keywords</legend>

                <div id="keywordsPanel">
                    <g:each in="${keywords}" status="i" var="kw">
                        <div>
                          %{--  <label for="keyword_${kw.id}">Keyword:</label> --}%
                            <input id="keyword_${kw.id}" name="keyword_${kw.id}" type="text" value="${kw.keyword}"/>
                           %{-- <label for="category_${kw.id}">Category:</label> --}%
                            <g:select name="category_${kw.id}" from="${categories}" value="${kw.category}"/>
                            %{--  <input id="category_${kw.id}" type="text" value="${kw.category}" readonly/> --}%
                            <button type="button" class="delete kwd" id="kwDelete_${kw.id}">Delete</button>
                        </div>
                    </g:each>
                </div>

                <div clas="buttons">
                    <button type="button" class="add" id="kwAdd">Add Keyword</button>
                    <button type="submit" class="edit" id="kwSave">Save Changes</button>
                </div>
            </fieldset>
        </div>

        <div id="keywords" class="panel">
            <fieldset>
                <legend>Existing Keywords</legend>

                <div id="ekPanel">
                    <g:each in="${existingKeywords}" status="i" var="kw">
                        <div>
                            <label for="ek_${kw.id}">Keyword:</label>
                            <input id="ek_${kw.id}" name="ek_${kw.id}" type="text" value="${kw.keyword}" readonly/>
                            <label for="ekc_${kw.id}">Category:</label>
                            <input id="ekc_${kw.id}_${kw.id}" name="ekc_${kw.id}" type="text" value="${kw.category}" readonly/>
                        </div>
                    </g:each>
                </div>
            </fieldset>
        </div>

        <div id="bounding_boxes" class="panel">

            <g:each in="${bbList}" status="i" var="bb">
                <div class="bb">

                    <fieldset>
                        <legend>Bounding Box</legend>

                        <div style="float:left; width:300px;">
                            <g:if test="${bb.text}">
                                <span>${bb.text}</span><br>
                            </g:if>
                            <label for="sl_${bb.id}">Latitude South:</label>
                            <input id="sl_${bb.id}" name="sl_${bb.id}" type="text" value="${bb.latSouth}"/>
                            <br>
                            <label for="wl_${bb.id}">Longitude West:</label>
                            <input id="wl_${bb.id}" name="wl_${bb.id}" type="text" value="${bb.lngWest}"/><br>
                            <label for="nl_${bb.id}">Latitude North:</label>
                            <input id="nl_${bb.id}" name="nl_${bb.id}" type="text" value="${bb.latNorth}"/><br>
                            <label for="el_${bb.id}">Longitude East:</label>
                            <input id="el_${bb.id}" name="el_${bb.id}" type="text" value="${bb.lngEast}"/><br>

                            <div clas="buttons">
                                <button type="submit" class="edit bbSave" id="bbSave_${bb.id}">Save Changes</button>
                                <button type="button" class="delete bbDel" id="bbDelete_${bb.id}">Delete</button>
                            </div>
                        </div>

                        <div style="float:left;">
                            <div id="map-canvas_${bb.id}" style="width:500px;height:380px;margin:0.5em;">
                            </div>
                        </div>
                        <br style="clear:left"/>
                    </fieldset>
                </div>

            </g:each>
        </div>
    </g:form>
    <div id="addKeywordTemplate" style="display: none">
        <div>
            <input id="keyword" name="keyword" type="text" value=""/>
            <g:select name="category" from="${categories}" />
            %{--
            <g:select name="category" from="${[
                    'Atmosphere', 'Chemical entity', 'Document',
                    'Environmental material', 'Equipment', 'Geographic location',
                    'Geosphere', 'Habitat', 'Human activity',
                    'Location', 'Material entity', 'Observed Property',
                    'Organization', 'Place', 'Process', 'Publication', 'Resource originator',
                    'Specification', 'Theme', 'Unknown', 'Water body', 'chemical process',
                    'dataCenter', 'dataResolution', 'dataset', 'discipline',
                    'instrument', 'place', 'platform', 'project', 'stratum', 'temporal', 'theme']}"/>
                    --}%
            <button type="button" class="delete">Delete</button>
        </div>
    </div>
</div>
</body>
</html>