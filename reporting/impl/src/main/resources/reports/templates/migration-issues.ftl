<!DOCTYPE html>

<#macro migrationIssuesRenderer problemSummary>
    <tr class="problemSummary effort${getEffortDescriptionForPoints(problemSummary.effortPerIncident, 'id')}">
        <td>
            <a href="#" class="toggle">
                <#assign issueName = problemSummary.issueName!"No name">
                ${issueName?html}
            </a>
        </td>
        <td class="text-right">${problemSummary.numberFound}</td>
        <td class="text-right">${problemSummary.effortPerIncident}</td>
        <td class="level">${getEffortDescriptionForPoints(problemSummary.effortPerIncident, 'verbose')}</td>
        <td class="text-right">${problemSummary.numberFound * problemSummary.effortPerIncident}</td>
    </tr>
    <tr class="tablesorter-childRow bg-info" data-summary-id="${problemSummary.id}">
        <td><div class="indent"><strong>File</strong></div></td>
        <td class="text-right"><strong>Incidents Found</strong></td>
        <td colspan="3"><strong>Hint</strong></td>
    </tr>
</#macro>

<#function getIncidentsFound problemSummaries>
    <#assign result = 0>
    <#list problemSummaries as problemSummary>
        <#assign result = result + problemSummary.numberFound>
    </#list>
    <#return result>
</#function>

<#function getTotalPoints problemSummaries>
    <#assign result = 0>
    <#list problemSummaries as problemSummary>
        <#assign result = result + (problemSummary.numberFound * problemSummary.effortPerIncident)>
    </#list>
    <#return result>
</#function>

<html lang="en">
    <#if reportModel.applicationReportIndexModel??>
        <#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>
    </#if>

    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <title>
            <#if reportModel.projectModel??>
                ${reportModel.projectModel.name} -
            </#if>
            ${reportModel.reportName}
        </title>
        <link href="resources/css/bootstrap.min.css" rel="stylesheet">
        <link href="resources/css/windup.css" rel="stylesheet" media="screen">
        <link href="resources/css/windup.java.css" rel="stylesheet" media="screen">
        <link href="resources/css/jquery-ui.min.css" rel="stylesheet" media="screen">
        <link href="resources/img/rhamt-icon-128.png" rel="shortcut icon" type="image/x-icon"/>
        <style>
            .table-bordered { border: 1px solid #222222;  border-collapse: collapse;}
            .table-bordered>tfoot>tr>th, .table-bordered>tfoot>tr>td .table-bordered>tbody>tr>th,  .table-bordered>thead>tr>th, .table-bordered>thead>tr>td, .table-bordered>tbody>tr>td {
              border: 1px inset #dddddd
            }
            .fileSummary {
                background-color: #f5f5f5;
            }
            /* Only horizontal lines. */
            .migration-issues-table.table-bordered > thead > tr > th,
            .migration-issues-table.table-bordered > tbody > tr > td {
                border-left-style: none;
                border-right-style: none;
            }

            /* Light yellow bg for the issue info box. */
            .hint-detail-panel > .panel-heading {
                border-color: #c2c2c2;
                background-color: #fbf4b1;
            }
            .hint-detail-panel {
                border-color: #c2c2c2;
                background-color: #fffcdc;
            }
            /* Reduce the padding, default is too big. */
            .hint-detail-panel > .panel-body { padding-bottom: 0; }

            /* Colors of various effort levels. */
            /* Commented out for now (jsight - 2016/02/15)
            tr.problemSummary.effortINFO td.level { color: #1B540E; }
            tr.problemSummary.effortTRIVIAL td.level { color: #50A40E; }
            tr.problemSummary.effortCOMPLEX td.level { color: #0065AC; }
            tr.problemSummary.effortREDESIGN td.level { color: #C67D00; }
            tr.problemSummary.effortARCHITECTURAL td.level { color: #C42F0E; }
            tr.problemSummary.effortUNKNOWN td.level { color: #C42F0E; }
            */
        </style>
    </head>
    <body role="document">
        <!-- Navbar -->
        <div id="main-navbar" class="navbar navbar-default navbar-fixed-top">
            <div class="wu-navbar-header navbar-header">
                <#include "include/navheader.ftl">
            </div>

            <#if applicationReportIndexModel??>
                <div class="navbar-collapse collapse navbar-responsive-collapse">
                    <#include "include/navbar.ftl">
                </div><!-- /.nav-collapse -->
            </#if>
        </div>
        <!-- / Navbar -->

        <div class="container-fluid" role="main">
            <div class="row">
                <div class="page-header page-header-no-border">
                    <h1>
                        <div class="main">${reportModel.reportName}</div>
                        <#if reportModel.projectModel??>
                            <div class="path">${reportModel.projectModel.rootFileModel.fileName}</div>
                        </#if>
                    </h1>
                    <div class="desc">
                        ${reportModel.description}
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="container-fluid theme-showcase" role="main">
                    <!-- HEAD -->
                    <#assign problemsBySeverity = getProblemSummaries(event, reportModel.projectModel, reportModel.includeTags, reportModel.excludeTags)>
                    <#if !problemsBySeverity?has_content>
                        <div>
                            No issues were found by the existing rules. If you would like to add custom rules,
                            see the <a href="https://github.com/windup/windup/wiki/Rules-Development-Guide">
                            Rule Development Guide</a>.
                        </div>
                    </#if>
                    <#list problemsBySeverity?keys as severity>
                        <table class="table table-bordered table-condensed tablesorter migration-issues-table">
                            <thead>
                                <tr class="tablesorter-ignoreRow" style="background: rgb(0, 140, 186);color: #FFFFFF; font-size: 14pt;">
                                    <td style="border: 0px; padding: 10px 15px"><b>${severity}</b></td>
                                    <td style="border: 0px"></td>
                                    <td style="border: 0px"></td>
                                    <td style="border: 0px"></td>
                                    <td style="border: 0px"></td>
                                </tr>
                                <tr style="background: rgb(212, 230, 233);">
                                    <th class="sortable">Issue by Category</th>
                                    <th class="sortable-right text-right">Incidents Found</th>
                                    <th class="sortable-right text-right">Story Points per Incident</th>
                                    <th>Level of Effort</th>
                                    <th class="sortable-right text-right">Total Story Points</th>
                                </tr>
                            </thead>
                            <tfoot>
                                <tr class="tablesorter-ignoreRow" style="background: rgb(212, 230, 233);">
                                    <td style="border: 0px"></td>
                                    <td style="border: 0px" class="text-right">${getIncidentsFound(problemsBySeverity[severity])}</td>
                                    <td style="border: 0px"></td>
                                    <td style="border: 0px"></td>
                                    <td style="border: 0px" class="text-right">${getTotalPoints(problemsBySeverity[severity])}</td>
                                </tr>
                            </tfoot>
                            <tbody>
                                  <#list problemsBySeverity[severity] as problemSummary>
                                      <@migrationIssuesRenderer problemSummary />
                                  </#list>
                            </tbody>
                        </table>
                    </#list>
                    <!-- 4c47fc7cb... Several style fixes to make the reports more consistent -->
                </div>
            </div>
            <#include "include/timestamp.ftl">
        </div>

        <script src="resources/js/jquery-1.10.1.min.js"></script>
        <script src="resources/js/jquery-ui.min.js"></script>
        <script src="resources/js/bootstrap.min.js"></script>
        <script src="resources/js/jquery.tablesorter.min.js"></script>
        <script src="resources/js/jquery.tablesorter.widgets.min.js"></script>
        <script src="resources/libraries/handlebars/handlebars.4.0.5.min.js"></script>

        <script type="text/javascript">
            $(document).ready(function() {

                var $table = $('.tablesorter');

                // hide child rows & make draggable
                $table.find('.tablesorter-childRow')
                    .find('td')
                    .droppable({
                        accept: '.draggingSiblings',
                        drop: function(event, ui) {
                            if ($(this).closest('tr').length){
                                $(this).closest('tr').before(
                                    ui.draggable
                                        .css({ left: 0, top: 0 })
                                        .parent()
                                        .removeClass('draggingRow')
                                    );
                                $table
                                    .find('.draggingSiblingsRow')
                                    .removeClass('draggingSiblingsRow')
                                    .find('.draggingSiblings')
                                    .removeClass('draggingSiblings');
                                $table.trigger('update');
                            } else {
                                return false;
                            }
                        }
                    })
                    .draggable({
                        revert: "invalid",
                        start: function( event, ui ) {
                            $(this)
                                .parent()
                                .addClass('draggingRow')
                                .prevUntil('.tablesorter-hasChildRow')
                                .nextUntil('tr:not(.tablesorter-childRow)')
                                .addClass('draggingSiblingsRow')
                                .find('td')
                                .addClass('draggingSiblings');
                        }
                    })
                    .hide();

                // we need these parsers because we are using comma to separate thousands and are also sorting links
                $.tablesorter.addParser({
                     id: 'thousands',
                     is: function(s) { return true; },
                     format: function(s) {
                         return s.replace('$','').replace(/,/g,'');
                     },
                     type: 'numeric'
                });
                $.tablesorter.addParser({
                    id: 'a-elements',
                    is: function(s) { return true; },
                    format: function(s)
                    {
                        // format your data for normalization
                        return s.replace(new RegExp(/<.*?>/),"");
                    },
                    parsed: true,
                    type: 'text'
                });

                $table
                    .tablesorter({
                        // this is the default setting
                        cssChildRow: "tablesorter-childRow",
                        sortList: [[1,1]],
                        headers: {
                            0: {sorter: 'a-elements'},
                            1: {sorter: 'thousands'},
                            2: {sorter: 'thousands'},
                            3: {sorter: false},
                            4: {sorter: 'thousands'},
                        }
                    })
                    .delegate('.toggle', 'click' ,function(){
                        $(this)
                            .closest('tr')
                            .nextUntil('tr.tablesorter-hasChildRow')
                            .find('td').first().each(function(index, element) { showDetails(element) });
                        return false;
                    });

            });

       	    function resizeTables()
            {
                var tableArr = document.getElementsByClassName('migration-issues-table');
                var cellWidths = new Array();

                // get widest
                for(i = 0; i < tableArr.length; i++)
                {
                    for(j = 0; j < tableArr[i].rows[0].cells.length; j++)
                    {
                       var cell = tableArr[i].rows[0].cells[j];

                       if(!cellWidths[j] || cellWidths[j] < cell.clientWidth)
                            cellWidths[j] = cell.clientWidth;
                    }
                }

                // set all columns to the widest width found
                for(i = 0; i < tableArr.length; i++)
                {
                    for(j = 0; j < tableArr[i].rows[0].cells.length; j++)
                    {
                        tableArr[i].rows[0].cells[j].style.width = cellWidths[j]+'px';
                    }
                }
            }

            window.onload = resizeTables;

        </script>

        <!#-- We are using short variable names because they repeat a lot in the generated HTML
              and using short reduces the file sizes significantly.
              
              {l} - label
              {oc} - occurences
              {h} - href
              {t} - link title

              The actual data are generated lower into MIGRATION_ISSUES_DETAILS[] .
        -->
        <#noparse>
        <script id="detail-row-template" type="text/x-handlebars-template">
            {{#each problemSummaries}}
                {{#each files}}
                    <tr class="fileSummary tablesorter-childRow fileSummary_id_{{../problemSummaryID}}">
                        <td>
                            <div class="indent">
                                {{{l}}}
                            </div>
                        </td>
                        <td class="text-right">
                            {{oc}}
                        </td>

                        {{#if @first}}
                            <td colspan="3" rowspan="{{../files.length}}">
                                <div class="panel panel-default hint-detail-panel">
                                    <div class="panel-heading">
                                        <h4 class="panel-title pull-left">Issue Detail: {{../issueName}}</h4>
                                        {{#if ../ruleID}}
                                            <div class="pull-right">
                                                <a class="sh_url" title="{{../ruleID}}" href="windup_ruleproviders.html#{{../ruleID}}">Show Rule</a>
                                            </div>
                                        {{/if}}
                                        <div class="clearfix"></div>
                                    </div>
                                    <div class="panel-body">
                                        {{{../description}}}
                                    </div>

                                    {{#if ../resourceLinks}}
                                        <div class="panel-body">
                                            <ul>
                                                {{#each ../resourceLinks}}
                                                    <li><a href="{{h}}" target="_blank">{{t}}</a></li>
                                                {{/each}}
                                            </ul>
                                        </div>
                                    {{/if}}
                                </div>
                            </td>
                        {{/if}}
                    </tr>
                {{/each}}
            {{/each}}
        </script>
        </#noparse>

        <script type="text/javascript">
            var issueDataLoaded = [];

            function showDetails(element) {
                var problemSummaryID = $(element).parent().attr("data-summary-id")
                var tr = $(element).parent();

                var issueDataArray = MIGRATION_ISSUES_DETAILS[problemSummaryID];
                if (!issueDataLoaded[problemSummaryID]) {
                    // append it and try again in a second
                    var script = document.createElement("script");
                    script.type = "text/javascript";
                    script.src = "data/problem_summary_" + problemSummaryID + ".js";
                    document.body.appendChild(script);

                    issueDataLoaded[problemSummaryID] = true;
                    setTimeout(function() { showDetails(element); }, 25);
                    return;
                } else if (issueDataArray == null) {
                    setTimeout(function() { showDetails(element); }, 25);
                    return;
                }

                function toggleRow () {
                    $(tr).find("td").toggle();
                    var issuesTable = $(element).parent().parent().parent();
                    $(issuesTable).trigger("update", [true]);
                }

                $(".fileSummary_id_" + problemSummaryID).remove();
                if ($(element).is(":visible")) {
                    toggleRow();
                    return;
                }

                var source   = $("#detail-row-template").html();
                var template = Handlebars.compile(source);
                var html = template({problemSummaries: issueDataArray});

                $(html).insertAfter(tr);

                toggleRow();
            }

            // summary in JS should go here
            var MIGRATION_ISSUES_DETAILS = [];
        </script>

        <#if problemsBySeverity?has_content>
            <#list problemsBySeverity?keys as severity>
                <#list problemsBySeverity[severity] as problemSummary>
                    <@write_to_disk filename="problem_summary_${problemSummary.id}.js">
                        <#compress>
                        MIGRATION_ISSUES_DETAILS["${problemSummary.id}"] = [
                        <#list problemSummary.descriptions as originalDescription>
                            <#assign description = originalDescription!"-- No detailed text --">
                            <#assign ruleID = problemSummary.ruleID!"">
                            <#assign issueName = problemSummary.issueName!"">
                            {description: "${markdownToHtml(description)?js_string}", ruleID: "${ruleID?js_string}", issueName: "${issueName?js_string}",
                            problemSummaryID: "${problemSummary.id}", files: [
                            <#list problemSummary.getFilesForDescription(originalDescription) as fileSummary>
                                <#--
                                    If this is an application specific report, then the report model will contain the
                                     correct application. In this case the non-canonical project will be used.

                                     If it is a global report, then the file model can be used to find the application associated
                                     with that file. In this case, the canonical local will be used.
                                -->
                                <#assign application = reportModel.projectModel!fileSummary.file.projectModel.rootProjectModel>

                                <#assign renderedLink><@render_link model=fileSummary.file project=application/></#assign>
                                {l:"${renderedLink?json_string}", oc:"${fileSummary.occurrences?json_string}"},
                            </#list>
                            ], resourceLinks: [
                                <#list problemSummary.links! as link>
                                {h:"${link.link?json_string}", t:"${link.title?json_string}"},
                                </#list>
                            ]},
                        </#list>
                        ];
                        </#compress>
                    </@write_to_disk>
                </#list>
            </#list>
        </#if>
    </body>
</html>
