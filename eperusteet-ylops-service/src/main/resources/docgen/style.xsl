<xsl:stylesheet
        version="1.0"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:fo="http://www.w3.org/1999/XSL/Format">
    <xsl:output indent="yes"/>
    <xsl:strip-space elements="*"/>

    <!-- Title page -->
    <xsl:template match="/book">
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
            <fo:layout-master-set>
                <fo:simple-page-master master-name="my-page" page-width="210mm" page-height="297mm">
                    <fo:region-body margin="20mm" margin-top="30mm"/>
                </fo:simple-page-master>
            </fo:layout-master-set>
            <fo:page-sequence master-reference="my-page">
                <fo:flow flow-name="xsl-region-body">
                    <fo:block text-align="center" font-size="28pt" font-weight="bold">
                        <xsl:value-of select="/book/info/title" />
                    </fo:block>
                    <fo:block text-align="center" font-size="18pt" font-weight="bold">
                        <xsl:value-of select="/book/info/subtitle" />
                    </fo:block>
                    <fo:block text-align="center" font-size="18pt" font-weight="bold">
                        <xsl:value-of select="/book/info/type" />
                    </fo:block>
                    <fo:block page-break-before="always" />
                </fo:flow>
            </fo:page-sequence>
            <fo:page-sequence master-reference="my-page">
                <fo:flow flow-name="xsl-region-body">
                    <fo:block text-align="center" font-size="28pt" font-weight="bold">
                        info
                    </fo:block>
                </fo:flow>
            </fo:page-sequence>
            <xsl:apply-templates/>
        </fo:root>
    </xsl:template>

    <!-- Template page -->

    <xsl:template match="page">

    </xsl:template>

    <!-- Info page -->

    <!-- Table of contents page -->

</xsl:stylesheet>