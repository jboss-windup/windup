<!DOCTYPE html>
<html lang="en">

<#if reportModel.sourceFileModel.boundProject??>
<#assign applicationReportIndexModel = projectModelToApplicationIndex(reportModel.sourceFileModel.boundProject)>
</#if>

<head>
    <meta charset="utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <title>Source Report for ${reportModel.reportName?html}</title>
    <link href="resources/css/bootstrap.min.css" rel="stylesheet"/>
    <link href="resources/css/windup.css" rel="stylesheet" media="screen"/>
    <link rel='stylesheet' type='text/css' href='resources/libraries/snippet/jquery.snippet.min.css' />
    <link rel='stylesheet' type='text/css' href='resources/css/windup-source.css' />
    <link rel='stylesheet' type='text/css' href='resources/libraries/sausage/sausage.css' />
</head>
<body role="document" class="source-report">

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
        </div><!-- /.nav-collapse -->
    </div>


    <div class="container-fluid" role="main">
        <div class="row">
            <div class="page-header page-header-no-border">
                <h1>
                    <div class="main"
                        onmouseover="$(this).parent().parent().addClass('showDesc')"
                        onmouseout=" $(this).parent().parent().removeClass('showDesc')"
                              >Source Report</div>
                    <div class="path">${reportModel.sourceFileModel.prettyPath?html}</div>
                </h1>
                <div class="desc">
                    This report displays what Windup found in individual files.
                    Each item is shown below the line it was found on,
                    and next to it, you may find a link to the rule which it was found by.
                </div>
            </div>
        </div>

        <div class="row">
            <!-- Breadcrumbs -->
            <div class="container-fluid">
                <ol class="breadcrumb top-menu">
                    <li><a href="../index.html">All Applications</a></li>
                    <#include "include/breadcrumbs.ftl">
                </ol>
            </div>
            <!-- / Breadcrumbs -->
        </div>


        <div class="row">
            <div class="container-fluid theme-showcase" role="main">

                <#if reportModel.sourceFileModel.classificationModels.iterator()?has_content || getTechnologyTagsForFile(reportModel.sourceFileModel).iterator()?has_content>
                <div class="panel panel-primary">
                    <div class="panel-heading">
                        <h3 class="panel-title">Information</h3>
                    </div>
                    <div class="panel-body" style="overflow: auto;">

                        <!--<div style="height: 120pt; float:left;"></div> Keeps the minimal height. -->
                        <div class="points" style="text-align: center; color: #00254b; padding-bottom: 1ex;">
                            <div class="number">${getMigrationEffortPointsForFile(reportModel.sourceFileModel)}</div>
                            <div>Story Points</div>
                        </div>

                        <div class="info" style="margin-left: 95pt;">

                            <#list getTechnologyTagsForFile(reportModel.sourceFileModel).iterator()>
                            <h4>Technologies</h4>
                            <div class="technologies" style="overflow: auto"><!-- "auto" to contain all the tags. -->
                                <#items as techTag>
                                    <span class="label label-info">${techTag.name}</span>
                                </#items>
                            </div>
                            </#list>

                            <#list reportModel.sourceFileModel.classificationModels.iterator()>
                                <ul class='classifications'>
                                    <#items as item>
                                        <#if item.classification??>
                                            <li>
                                                <div class='title'>
                                                    <em>${item.classification!}</em>
                                                    <@render_rule_link renderType='glyph' ruleID=item.ruleID class='rule-link'/><#-- Link to the rule -->
                                                </div>
                                                <#if item.description??><div class='desc'>${item.description}</div></#if>
                                                <@render_linkable linkable=item layout='ul'/><#-- Link contained in classification -->
                                            </li>
                                        </#if>
                                    </#items>
                                </ul>
                            </#list>

                            <#list reportModel.sourceFileModel.linksToTransformedFiles.iterator() >
                            <h4>Automatically Translated Files</h4>
                            <ul>
                                <#items as link>
                                    <li><a href="${link.link}">${link.description!}</a></li>
                                </#items>
                            </ul>
                            </#list>

                            <div style="clear: both;"/><!-- Snaps under the height keeper. Yes, the same effect could be achieved by a table. -->
                        </div><!-- .info -->
                    </div>
                </div>
                </#if>



                <pre id='source'><#t>
                    ${reportModel.sourceBody?html}<#t>
                </pre><#t>

            </div> <!-- /container -->
        </div><!-- /row-->
    </div><!-- /container main-->

    <script src="resources/js/jquery-1.7.min.js"></script>
    <script src="resources/js/bootstrap.min.js"></script>

    <script type='text/javascript' src='resources/libraries/jquery-ui/jquery.ui.widget.js'></script>
    <script type='text/javascript' src='resources/libraries/snippet/jquery.snippet.min.js'></script>
    <script type='text/javascript' src='resources/libraries/snippet/jquery.snippet.java-properties.js'></script>
    <script type='text/javascript' src='resources/libraries/snippet/jquery.snippet.java-manifest.js'></script>
    <script type='text/javascript' src='resources/libraries/sausage/jquery.sausage.min.js'></script>



    <script type='text/javascript'>
        $(window).on("hashchange", function () {
            window.scrollTo(window.scrollX, window.scrollY - 50);
        });
        function offsetAnchor() {
            if(location.hash.length !== 0) {
                window.scrollTo(window.scrollX, window.scrollY - 50);
            }
        }
        window.setTimeout(function() {
            offsetAnchor();
        }, 1);
        $(document).ready(function(){
            $('pre').snippet('${reportModel.sourceType}',{style:'ide-eclipse', showNum:true,boxFill:'#ffeeb9', box: '${reportModel.sourceBlock}' });

        <#list reportModel.sourceFileModel.inlineHints.iterator() as hintLine>
            <#assign lineNumber = hintLine.lineNumber>
            $("<div id='${lineNumber?c}-inlines' class='inline-source-hint-group'/>").appendTo('ol.snippet-num li:nth-child(${lineNumber?c})');
        </#list>

        <#list reportModel.sourceFileModel.inlineHints.iterator() as hintLine >
            <#assign lineNumber = hintLine.lineNumber>
            <#assign hintClasses = hintLine.tags?join(" tag-","none")>

            $("<a name='${hintLine.asVertex().getId()?c}' class='windup-file-location'></a><#t>
                <div class='inline-source-comment green tag-${hintClasses}'><#t>
                    <#if hintLine.hint?has_content>
                        <div class='inline-comment'><#t>
                            <div class='inline-comment-heading'><#t>
                                <strong class='notification ${effortPointsToCssClass(hintLine.effort)}'><#t>
                                    ${hintLine.title?js_string}<#t>
                                </strong><#t>
                                <@render_rule_link renderType='glyph' ruleID=hintLine.ruleID class='rule-link floatRight'/><#t>
                                <#t>
                            </div><#t>
                            <div class='inline-comment-body'><#t>
                                ${markdownToHtml(hintLine.hint)?js_string}<#t>
                                <#if hintLine.links?? && hintLine.links.iterator()?has_content>
                                        <ul><#t>
                                            <#list hintLine.links.iterator() as link>
                                                <li><#t>
                                                    <a href='${link.link}'>${link.description}</a><#t>
                                                </li><#t>
                                            </#list>
                                        </ul><#t>
                                </#if>
                            </div><#t>
                        </div><#t>
                    </#if>
                </div><#t>
            ").appendTo('#${lineNumber?c}-inlines');<#t>

        </#list>


            $('code[class]').each(function(){
                 var codeSyntax = ($(this).attr('class'));
                 if(codeSyntax) {
                    $(this).parent().snippet(codeSyntax,{style:'ide-eclipse', menu:false, showNum:false});
                 }
            });
            $(window).sausage({ page: 'li.box' });
        });
    </script>

</body>
</html>
