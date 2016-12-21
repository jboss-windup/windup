<!DOCTYPE html>
<html lang="en">

<#if reportModel.applicationReportIndexModel??>
    <#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>
</#if>


<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>${reportModel.projectModel.rootFileModel.fileName?html} - ${reportModel.reportName}</title>
    <link href="resources/css/bootstrap.min.css" rel="stylesheet">
    <link href="resources/css/windup.css" rel="stylesheet" media="screen">
    <link rel="stylesheet" type="text/css" href="resources/libraries/snippet/jquery.snippet.min.css" />
    <link href="resources/css/windup.java.css" rel="stylesheet" media="screen">
    <link rel="stylesheet" type="text/css" href="resources/libraries/sausage/sausage.css" />
    <link rel="stylesheet" type="text/css" href="resources/libraries/flot/plot.css" />
    <link href="resources/img/favicon.png" rel="shortcut icon" type="image/x-icon"/>
    <style>
        .report-index-row {
            margin: 10px -32px 0px 5px;
            margin-bottom: 25px;
        }
        .dataTable { max-width: 500px; margin-top: 2ex; }
    </style>
</head>
<body role="document" class="java-report-index">

    <!-- Navbar -->
    <div id="main-navbar" class="navbar navbar-default navbar-fixed-top">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-responsive-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
        </div>

        <div class="navbar-collapse collapse navbar-responsive-collapse">
            <#include "include/navbar.ftl">
        </div><!-- /.nav-collapse -->
    </div>
    <!-- / Navbar -->

    <div class="container-fluid" role="main">

        <div class="row">
            <div class="page-header page-header-no-border">
                <h1>
                    <div class="main" onmouseover="$(this).parent().parent().addClass('showDesc')" onmouseout=" $(this).parent().parent().removeClass('showDesc')">
                        ${reportModel.reportName}
                    </div>
                    <div class="path">${reportModel.projectModel.rootFileModel.fileName}</div>
                </h1>
                <div class="desc" style="z-index: 9000">
                    ${reportModel.description}
                </div>
            </div>
        </div>

        <div class="row container-fluid summaryInfo">
            <div class="panel panel-primary col-md-12">

                <div class="row report-index-row">
                    <!-- Incidents and Story Points -->
                    <div class="col-md-4" style="max-width: 560px;">
                        <div class="chart">
                            <div style="text-align: center"><strong>Incidents and Story Points</strong></div>
                            <div id="effortAndSeverityChart"></div>
                        </div>
                        <div class="dataTable">
                            <table class="table table-condensed table-striped" id="incidentsByTypeTable">
                                <thead>
                                    <tr>
                                        <td>
                                            <b>Incidents by Category</b>
                                        </td>
                                        <td class='numeric-column'>
                                            <b>Incidents</b>
                                        </td>
                                        <td class='numeric-column'>
                                            <b>Total Story Points</b>
                                        </td>
                                    </tr>
                                </thead>
                                <tbody id="incidentsByTypeTBody">
                                </tbody>
                            </table>
                        </div>
                    </div>
                    <!-- Mandatory Incidents and Story Points -->
                    <div class="col-md-4" style="max-width: 560px;">
                        <div class="chart">
                            <div style="text-align: center"><strong>Mandatory Incidents and Story Points</strong></div>
                            <div id="mandatoryIncidentsByEffortAndStoryPoints"></div>
                        </div>
                        <div class="dataTable">
                            <table class="table table-condensed table-striped">
                                <thead>
                                    <tr>
                                        <td>
                                            <b>Mandatory Incidents by Type</b>
                                        </td>
                                        <td class='numeric-column'>
                                            <b>Incidents</b>
                                        </td>
                                        <td class='numeric-column'>
                                            <b>Total Story Points</b>
                                        </td>
                                    </tr>
                                </thead>
                                <tbody id="mandatoryIncidentsByEffortTBody">
                                </tbody>
                            </table>
                        </div>
                    </div>
                    <!--  Java Incidents by Package -->
                    <div class="col-md-4" style="max-width: 560px;">
                        <div class="chart" style="height: 306px;">
                            <div style="margin-bottom: 15px; margin-left: 48px;">
                                <strong>Java Incidents by Package</strong>
                            </div>
                            <div id='application_pie' class='windupPieGraph'></div>
                        </div>
                        <div class="dataTable">
                            <table class="table table-condensed table-striped">
                                <thead>
                                    <tr>
                                        <td> <b>Java Incidents by Package</b> </td>
                                        <td class='numeric-column'> <b>Incidents</b> </td>
                                    </tr>
                                </thead>
                                <tbody id="javaIncidentsByPackageTBody">
                                </tbody>
                            </table>
                            <span class="note">Note: this does not include XML files and "possible" issues.</span>
                        </div>
                    </div>
                </div><#-- .row -->

            </div><#-- .panel -->
        </div><#-- .row.summaryInfo -->

        <div class="row container-fluid additionalReports">
            <div class="panel panel-primary">
                <div class="panel-heading">
                    Additional Reports
                </div>
                <table class="table table-condensed table-striped">
                    <tbody>
                        <#list applicationReportIndexModel.applicationReportModelsSortedByPriority as navReportModel>
                            <#if navReportModel.displayInApplicationReportIndex>
                                <#assign reportUrl = navReportModel.reportFilename>
                                <#if navUrlPrefix??>
                                    <#assign reportUrl = "${navUrlPrefix}${reportUrl}">
                                </#if>

                                <#if !reportModel.equals(navReportModel)>
                                    <tr>
                                        <td class="col-md-2">
                                            <a href="${reportUrl}">${navReportModel.reportName}</a>
                                        </td>
                                        <td class="col-md-10">
                                            ${navReportModel.description!""}
                                        </td>
                                    </tr>
                                </#if>
                            </#if>
                        </#list>
                    </tbody>
                </table>
            </div>
        <#include "include/timestamp.ftl">
        </div>
    </div>

    <script type="text/javascript" src="resources/js/jquery-1.10.1.min.js"></script>
    <script src="resources/js/jquery.color-2.1.2.min.js"></script>
    <script src="resources/js/bootstrap.min.js"></script>
    <script src="resources/libraries/flot/jquery.flot.js"></script>
    <script src="resources/libraries/flot/jquery.flot.pie.min.js"></script>
    <script src="resources/libraries/flot/jquery.flot.valuelabels.js"></script>
    <script src="resources/libraries/flot/jquery.flot.axislabels.js"></script>
    <script src="resources/libraries/flot/jquery.flot.resize.js"></script>

    <script type="text/javascript" src="data/issue_summaries.js"></script>

    <script type="text/javascript">
        function getWindupIssueSummaries() {
            return WINDUP_ISSUE_SUMMARIES['${reportModel.projectModel.asVertex().id?c}'];
        }
    </script>

    <script type="text/javascript" src="resources/js/report-index-graphs.js"></script>

    <@render_pie projectTraversal=getProjectTraversal(reportModel.projectModel, 'only_once') recursive=true elementID="application_pie" includeTags=reportModel.includeTags excludeTags=reportModel.excludeTags />

    <script type="text/javascript">
        var tbodyElement = $("#javaIncidentsByPackageTBody");

        <#-- Make sure that the data exists before trying to use it -->
        if (typeof(WINDUP_PACKAGE_PIE_DATA) !== 'undefined') {
            var rows = "";
            for (var i = 0; i < WINDUP_PACKAGE_PIE_DATA['application_pie'].length; i++) {
                var row = "";
                row += "<tr>";
                row += "<td>" + WINDUP_PACKAGE_PIE_DATA['application_pie'][i].label + "</td>";
                row += "<td class='numeric-column'>" + WINDUP_PACKAGE_PIE_DATA['application_pie'][i].data + "</td>";
                row += "</tr>";
                rows += row;
            }
            tbodyElement.prepend(rows);
        } else {
            $("#javaIncidentsByPackageRow").remove();
        }
    </script>
</body>
</html>
