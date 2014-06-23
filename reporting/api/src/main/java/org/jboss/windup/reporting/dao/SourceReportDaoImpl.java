package org.jboss.windup.reporting.dao;

import com.tinkerpop.blueprints.Direction;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.ResourceModel;
import org.jboss.windup.graph.dao.impl.BaseDaoImpl;
import org.jboss.windup.reporting.meta.SourceReportModel;


public class SourceReportDaoImpl extends BaseDaoImpl<SourceReportModel> implements SourceReportDao
{
    public SourceReportDaoImpl()
    {
        super(SourceReportModel.class);
    }

    public boolean hasSourceReport(ResourceModel resource)
    {
        return resource.asVertex().getVertices(Direction.OUT, "sourceReport").iterator().hasNext();
    }

    public FileModel getResourceReport(ResourceModel resource)
    {
        if( ! hasSourceReport(resource) )
            return null;

        SourceReportModel report = getContext().getFramed().frame(
            resource.asVertex().getVertices(Direction.OUT, "sourceReport").iterator().next(),
            SourceReportModel.class);
        return report.getReportFile();
    }
}
