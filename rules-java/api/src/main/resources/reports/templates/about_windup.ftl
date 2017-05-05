<!DOCTYPE html>
<html lang="en">

<#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>

<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>
        <#if reportModel.projectModel??>
            ${reportModel.projectModel.name} - About
        <#else>
            About ${getWindupBrandName()}
        </#if>
    </title>
    <link href="resources/css/bootstrap.min.css" rel="stylesheet">
    <link href="resources/css/windup.css" rel="stylesheet" media="screen">
    <link href="resources/css/windup.java.css" rel="stylesheet" media="screen">
    <link href="resources/img/rhamt-icon-128.png" rel="shortcut icon" type="image/x-icon"/>
</head>
<body role="document">

    <!-- Navbar -->
    <div class="navbar navbar-default navbar-fixed-top">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-responsive-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
        </div>
        <div class="navbar-collapse collapse navbar-responsive-collapse">
            <#include "include/navbar.ftl">
        </div>
    </div>
    <!-- / Navbar -->


    <div class="container-fluid" role="main">
        <div class="row">
            <div class="page-header page-header-no-border">
                <h1>
                    <div class="main">About</div>
                </h1>
            </div>
        </div>

        <div class="row">
            <div class="container-fluid theme-showcase" role="main">
                <div class="panel panel-primary">
                    <div class="panel-heading">
                        <h3 class="panel-title">About ${getWindupBrandName()}</h3>
                    </div>

                    <div class="panel-body">
                        <dl class="dl-horizontal">
                            <#if reportModel.relatedResources.windupAbout.windupRuntimeVersion?has_content>
                                <dt>Generated By</dt>
                                <dd>${getWindupBrandName()} ${reportModel.relatedResources.windupAbout.windupRuntimeVersion}</dd>
                            </#if>
                            <dt>Follow on Twitter</dt>
                            <dd><a href="http://twitter.com/jbosswindup">@jbossWindup</a></dd>

                            <dt>Website</dt>
                            <dd><a href="https://developers.redhat.com/products/rhamt/overview/">https://developers.redhat.com/products/rhamt/overview/</a></dd>

                            <dt>Documentation</dt>
                            <dd><a href="https://access.redhat.com/documentation/en/red-hat-application-migration-toolkit/">https://access.redhat.com/documentation/en/red-hat-application-migration-toolkit/</a></dd>
                        </dl>

                        <dl class="dl-horizontal">
                            <dt>GitHub Source</dt>
                            <dd><a href="https://github.com/windup/windup">https://github.com/windup/windup</a></dd>

                            <dt>GitHub Wiki</dt>
                            <dd><a href="https://github.com/windup/windup/wiki">https://github.com/windup/windup/wiki</a></dd>

                            <dt>Discussion Forum</dt>
                            <dd><a href="https://community.jboss.org/en/windup?view=discussions">https://community.jboss.org/en/windup</a></dd>

                            <dt>Mailing List</dt>
                            <dd><a href="https://lists.jboss.org/mailman/listinfo/windup-dev">https://lists.jboss.org/mailman/listinfo/windup-dev</a></dd>

                            <dt>Issues Tracking</dt>
                            <dd><a href="https://issues.jboss.org/browse/WINDUP">https://issues.jboss.org/browse/WINDUP</a></dd>

                        </dl>
                    </div>
                </div>

            </div>
        </div>

        <div class="row">
            <div class="container-fluid theme-showcase" role="main">
                <div class="panel panel-primary">
                    <div class="panel-heading">
                        <h3 class="panel-title">Contributors</h3>
                    </div>
                    <div class="panel-body">
                        <div id="windup-contributors"></div>
                        <p class="windup-contributors small">A special <strong>thanks</strong> to our contributors!</p>
                    </div>
                </div>
            </div>
        </div>

        <#include "include/timestamp.ftl">
    </div> <!-- /container -->


    <script src="resources/js/jquery-1.10.1.min.js"></script>
    <script src="resources/libraries/flot/jquery.flot.min.js"></script>
    <script src="resources/libraries/flot/jquery.flot.pie.min.js"></script>
    <script src="resources/js/bootstrap.min.js"></script>

    <script type="text/javascript">
        var divTarget = $("#windup-contributors");
        $.getJSON( "https://api.github.com/repos/windup/windup/contributors", function( data ) {
          $.each( data, function( key, val ) {
            $( "<a data-toggle='tooltip' title='"+val.login+"' href='"+val.html_url+"'><img class='windup-contributors' src='"+val.avatar_url+"'/></a>").appendTo(divTarget);
          });
        });

        $('[data-toggle="tooltip"]').tooltip({
            'placement': 'top'
        });
    </script>
</body>
</html>
