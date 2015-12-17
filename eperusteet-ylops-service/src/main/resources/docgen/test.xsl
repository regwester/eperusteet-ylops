<xsl:stylesheet
        version="1.0"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:fo="http://www.w3.org/1999/XSL/Format"
        xmlns:fox="http://xmlgraphics.apache.org/fop/extensions">
    <xsl:output indent="yes"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="/test">
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
            <fo:layout-master-set>
                <fo:simple-page-master master-name="my-page" page-width="8.5in" page-height="11in">
                    <fo:region-body margin="1in" margin-top="1.5in"/>
                </fo:simple-page-master>
            </fo:layout-master-set>
            <fo:page-sequence master-reference="my-page">
                <fo:flow flow-name="xsl-region-body">
                    <xsl:call-template name="asd"/>
                </fo:flow>
            </fo:page-sequence>
            <xsl:apply-templates/>
        </fo:root>
    </xsl:template>

    <xsl:template name="asd">
        <fo:block break-before='page'>
            <fo:block font-size="16pt" font-weight="bold">TABLE OF CONTENTS</fo:block>
            <xsl:for-each select="//asd">
                <fo:block text-align-last="justify">
                </fo:block>
            </xsl:for-each>
        </fo:block>
    </xsl:template>
</xsl:stylesheet>