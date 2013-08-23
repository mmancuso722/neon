<!DOCTYPE html>
<html>
<head>
    <%
        String neonServerUrl = getServletContext().getInitParameter("neon.url");
        String owfServerUrl = getServletContext().getInitParameter("owf.url");
    %>

    <title>Aperture Map Display</title>

    <link rel="stylesheet" type="text/css" href="css/bootstrap/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="css/map.css">
    <link rel="stylesheet" type="text/css" href="<%=neonServerUrl%>/css/neon.css">

    <script src="<%=owfServerUrl%>/js/owf-widget.js"></script>
    <script src="<%=neonServerUrl%>/js/neon.js"></script>

    <script src="js/aperture-map.js"></script>

    <script>
        OWF.relayFile = 'js/eventing/rpc_relay.uncompressed.html';
        neon.query.SERVER_URL = '<%=neonServerUrl%>';
        neon.util.AjaxUtils.useDefaultStartStopCallbacks();
    </script>

    <script>
        $(document).ready(function() {
            $("#toggle").click(function () {
                $("#options").slideToggle("slow");

                if ($("#toggle-image").attr('src') == "img/arrow_down.png") {
                    $("#toggle-image").attr(
                            'src',
                            $("#toggle-image").attr('src').replace('_down', '_right')
                    );
                } else {
                    $("#toggle-image").attr(
                            'src',
                            $("#toggle-image").attr('src').replace('_right', '_down')
                    );
                }
            });
        });
    </script>

</head>
<body>
    <div class="container">
    <div id="options-bar">
        <div id="toggle">
            <img id="toggle-image" src="img/arrow_down.png" />
        </div>

        <div id="options">
            <div class="controls-row">
                <div class="control-group">
                    <label class="control-label" for="latitude">Latitude Field</label>

                    <div class="controls">
                        <select id="latitude" class="dropdown"></select>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label" for="longitude">Longitude Field</label>

                    <div class="controls">
                        <select id="longitude" class="dropdown"></select>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label" for="size-by">Size By</label>

                    <div class="controls">
                        <select id="size-by" class="dropdown"></select>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label" for="color-by">Color By</label>

                    <div class="controls">
                        <select id="color-by" class="dropdown"></select>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div id="map"></div>

    </div>

</body>
</html>