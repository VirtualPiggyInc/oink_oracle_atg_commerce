<%-- This page shows Virtual Piggy Child selection options to logged in Virtual Piggy Parent. --%>
<dsp:page>
  <dsp:importbean bean="/OriginatingRequest" var="originatingRequest"/>
  <dsp:importbean bean="/atg/userprofiling/ProfileFormHandler"/>
  <dsp:importbean bean="/atg/userprofiling/Profile"/>
  
  <crs:pageContainer index="false" 
                     follow="false" bodyClass="atg_store_pageLogin atg_store_checkout" redirectURL="../cart/cart.jsp">
    <crs:checkoutContainer currentStage=""
                           title="Select Virtual Piggy Child">
    <jsp:body>
    <div class="atg_store_main">
		<%-- show form errors --%>
		<dsp:getvalueof var="formExceptions" vartype="java.lang.Object" bean="ProfileFormHandler.formExceptions"/>
		<c:if test="${not empty formExceptions}">
			<c:forEach var="formException" items="${formExceptions}">
				<div style="text-align:center; font-weight:bold; font-size:11px;background-color:#ff9900;color:#ffffff;">
					${formException.message}
				</div>
			</c:forEach>
		</c:if>	
      <div class="atg_store_generalMessage">		
		<h3>Select a Child for Checkout</h3>
		
		<dsp:getvalueof var="vpSelectedChildId" vartype="java.lang.String" bean="/atg/userprofiling/Profile.virtualPiggySelectedChildId"/>
        
		<dsp:form id="atg_store_checkoutSelVPChildForm" formid="vpSelChild" action="${originatingRequest.requestURI}" method="post">
			<%-- loop over the list--%>
			<dsp:select bean="ProfileFormHandler.value.virtualPiggyChildSelect" nodefault="true">
				<dsp:option value="">Select a child for checkout</dsp:option>
				<%-- child select option --%>
			<dsp:droplet name="/atg/dynamo/droplet/ForEach">
				<dsp:param name="array" bean="/atg/userprofiling/Profile.virtualPiggyChildren"/>
				<dsp:oparam name="output">
				 <dsp:getvalueof var="childid" param="element.childId"/>
				 <c:choose>
				   <c:when test="${vpSelectedChildId==childid}">
					<dsp:option value='${childid}' selected="true"><dsp:valueof param="element.name"/></dsp:option>
				   </c:when>
				   <c:otherwise>
				     <dsp:option value='${childid}' selected="false"><dsp:valueof param="element.name"/></dsp:option>
				   </c:otherwise>
				 </c:choose>
				</dsp:oparam>
			</dsp:droplet>
			</dsp:select>
			<br/>
			<br/>
			<div align="center">
			<dsp:input type="checkbox" bean="ProfileFormHandler.value.virtualPiggyParentShipAddressSelection" value="false" checked="false"></dsp:input>
			Use Parent Address as Shipping Address
			</div>
			<div class="atg_store_formFooter">
			<div class="atg_store_formActions">
			  <span class="atg_store_basicButton">
				<dsp:input bean="ProfileFormHandler.selectVirtualPiggyChildSuccessURL" type="hidden" value="shipping.jsp"/>
				<dsp:input bean="ProfileFormHandler.selectVirtualPiggyChildErrorURL" type="hidden" beanvalue="/OriginatingRequest.requestURI"/>
				<dsp:input bean="ProfileFormHandler.selectVirtualPiggyChild" type="submit" value="Continue"/>
			  </span>
			</div>
			</div>
		</dsp:form>
		</div>
	</div>
    </jsp:body>
    </crs:checkoutContainer>

  </crs:pageContainer>
</dsp:page>
<%-- @version $Id: //hosting-blueprint/B2CBlueprint/version/10.0.3/Storefront/j2ee/store.war/checkout/loginPage.jsp#2 $$Change: 651448 $--%>
