<%
/**
 * OpenCPS is the open source Core Public Services software
 * Copyright (C) 2016-present OpenCPS community
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
%>
<%@page import="com.liferay.portlet.documentlibrary.service.DLAppLocalServiceUtil"%>
<%@page import="com.liferay.portal.service.UserLocalServiceUtil"%>
<%@page import="org.opencps.backend.util.PaymentRequestGenerator"%>
<%@page import="com.liferay.portal.kernel.util.ListUtil"%>
<%@page import="java.util.List"%>
<%@page import="org.opencps.util.DateTimeUtil"%>
<%@page import="com.liferay.portal.kernel.language.LanguageUtil"%>
<%@page import="java.text.DecimalFormat"%>
<%@page import="com.liferay.portlet.documentlibrary.util.DLUtil"%>
<%@page import="com.liferay.portal.kernel.repository.model.FileVersion"%>
<%@page import="com.liferay.portlet.documentlibrary.NoSuchFileEntryException"%>
<%@page import="com.liferay.portlet.documentlibrary.service.DLAppServiceUtil"%>
<%@page import="com.liferay.portal.kernel.repository.model.FileEntry"%>
<%@page import="org.opencps.util.MessageKeys"%>
<%@page import="org.opencps.paymentmgt.search.PaymentFileDisplayTerms"%>
<%@page import="java.text.NumberFormat"%>
<%@page import="org.opencps.usermgt.NoSuchWorkingUnitException"%>
<%@page import="org.opencps.usermgt.service.WorkingUnitLocalServiceUtil"%>
<%@page import="org.opencps.usermgt.model.WorkingUnit"%>
<%@page import="java.text.Format"%>
<%@page import="org.opencps.util.PortletUtil"%>
<%@page import="org.opencps.dossiermgt.service.DossierLocalServiceUtil"%>
<%@page import="com.liferay.portal.kernel.util.HtmlUtil"%>
<%@page import="org.opencps.paymentmgt.model.impl.PaymentFileImpl"%>
<%@page import="org.opencps.paymentmgt.service.PaymentFileLocalServiceUtil"%>
<%@page import="org.opencps.paymentmgt.model.PaymentFile"%>

<%@ include file="../init.jsp"%>

<style>
.lookup-result table {
    width: 100%;
}

.lookup-result tr td {
    padding: 5px;
    border: 1px solid #cbcbcb;
}
</style>
<%
	String backRedirect = ParamUtil.getString(request, "redirect");
	long paymentFileId = ParamUtil.getLong(request, "paymentFileId");
	PaymentFile paymentFile = PaymentFileLocalServiceUtil.fetchPaymentFile(paymentFileId);
	String soHoSo = StringPool.BLANK;
	String coQuanQuanLyHoaDon = StringPool.BLANK;
	Dossier dossier = null;
	ServiceInfo serviceInfo = null;
	
	if (Validator.isNull(paymentFile)) {
		paymentFile = new PaymentFileImpl();
	}
	else {
		soHoSo =
			DossierLocalServiceUtil.fetchDossier(
				paymentFile.getDossierId()).getReceptionNo();
		try {
			WorkingUnit wunit =
				WorkingUnitLocalServiceUtil.fetchByMappingOrganisationId(
					themeDisplay.getScopeGroupId(),
					paymentFile.getGovAgencyOrganizationId());
			if (wunit != null)
				coQuanQuanLyHoaDon = wunit.getName();
		}
		catch (Exception e) {

		}
		dossier =
			DossierLocalServiceUtil.fetchDossier(paymentFile.getDossierId());
		serviceInfo =
			ServiceInfoLocalServiceUtil.fetchServiceInfo(dossier.getServiceInfoId());
	}
	
	DecimalFormat df2 = new DecimalFormat("#,###,###,##0.00");
%>

<liferay-ui:header
	backURL="<%= backRedirect %>"
	title="payment-confirm"
	cssClass="upercase"
/>

<c:choose>
	<c:when test="<%= !StringPool.BLANK.equals(coQuanQuanLyHoaDon) %>">
	
		<liferay-ui:success 
			key="<%= MessageKeys.PAYMENT_FILE_CONFIRM_CASH_SUCCESS %>" 
			message="payment.file.confirm.cash.success"
		/>
		
		<liferay-ui:error 
			key="<%= MessageKeys.PAYMENT_FILE_CONFIRM_CASH_ERROR %>" 
			message="payment.file.confirm.cash.error" 
		/>
	
		<portlet:actionURL 
			var="confirmPaymentRequestedURL" 
			windowState="normal" 
			name="confirmPaymentRequested"
		/>
	
		<div class="payment-ld">
			<div class="content">
			
				<aui:form name="payForm" action="<%= confirmPaymentRequestedURL.toString() %>">
					<aui:input 
						type="hidden" 
						name="<%= PaymentFileDisplayTerms.PAYMENT_FILE_ID %>" 
						value="<%= String.valueOf(paymentFileId) %>"
					/>
					
					<div class="box100">
						<div>
		                	<p>
		                		<span><liferay-ui:message key="subject-name"/>:</span> 
		                		<%=Validator.isNotNull(dossier)? HtmlUtil.escape(dossier.getSubjectName()): "-" %>
		                	</p>
		                </div>
						<div>
							<p>
								<span>
								<liferay-ui:message key="reception-no"/>:
								</span> 
								<%= Validator.isNotNull(soHoSo) ?HtmlUtil.escape(soHoSo): "-" %>
							</p>
						</div>
					
						<div class="over100">
							<p>
								<span><liferay-ui:message key="thu-tuc-hanh-chinh"/>:</span> 
								<span>
								<%= Validator.isNotNull(serviceInfo) ?HtmlUtil.escape(serviceInfo.getServiceName()): "-" %>
								</span>
							</p>
						</div>
						
						<div>
							<p>
								<span><liferay-ui:message key="co-quan-thuc-hien"/>:</span>
								<%= Validator.isNotNull(coQuanQuanLyHoaDon) ? HtmlUtil.escape(coQuanQuanLyHoaDon) : "-"%>
							</p>
						</div>
						
						<div>
							<p>
								<span><liferay-ui:message key="ten-phi-thanh-toan"/>:</span> 
								<%= Validator.isNotNull(paymentFile) ? HtmlUtil.escape(paymentFile.getPaymentName()): "-" %>
							</p>
						</div>
						
						<div>
							<p>
								<span><liferay-ui:message key="ngay-yeu-cau"/>:</span>
								<%=Validator.isNotNull(paymentFile.getRequestDatetime())?HtmlUtil.escape(DateTimeUtil.convertDateToString(paymentFile.getRequestDatetime(), DateTimeUtil._VN_DATE_TIME_FORMAT)): "-" %>
							</p>
						</div>
						
						<div>
							<p>
								<span><liferay-ui:message key="so-tien"/>: </span> 
								<span class="red"><%=HtmlUtil.escape(df2.format(Double.valueOf(paymentFile.getAmount())).toString()) %> 
									<liferay-ui:message key="vnd"/>
								</span>
							</p>
						</div>
						
						<div>
							<p>
								<span><liferay-ui:message key="payment-method"/>: </span> 
								
								<c:choose>
		                           	<c:when test="<%= paymentFile.getPaymentMethod() == PaymentMgtUtil.PAYMENT_METHOD_CASH %>">
										<liferay-ui:message key="cash"></liferay-ui:message>
									</c:when>
									<c:when test="<%= paymentFile.getPaymentMethod() == PaymentMgtUtil.PAYMENT_METHOD_KEYPAY %>">
										<liferay-ui:message key="keypay"></liferay-ui:message>
									</c:when>
									<c:when test="<%= paymentFile.getPaymentMethod() == PaymentMgtUtil.PAYMENT_METHOD_BANK %>">
										<liferay-ui:message key="bank"></liferay-ui:message>
									</c:when>
		                           	<c:otherwise>
		                           		-
		                           	</c:otherwise>
		                        </c:choose>
							</p>
						</div>
						
						<div>
							<p>
							<span><liferay-ui:message key="request-note"/>:</span> 
							<%= Validator.isNotNull(paymentFile) ?HtmlUtil.escape(paymentFile.getRequestNote()): "-" %>
							</p>
						</div>
						
						<div>
							<p><span><liferay-ui:message key="chung-tu-kem-theo"/>:</span> 
							<%
							 	FileEntry fileEntry = null;
				 				try {
				 					fileEntry =
				 						DLAppLocalServiceUtil.getFileEntry(paymentFile.getConfirmFileEntryId());
				 				}
				 				catch (NoSuchFileEntryException e) {
				
				 				}
				 				String dlURL = null;
				 				
				 				if (fileEntry != null) {
				 					FileVersion fileVersion =
				 						fileEntry.getFileVersion();
				
				 					String queryString = "";
				 					boolean appendFileEntryVersion = true;
				
				 					boolean useAbsoluteURL = true;
				
				 					dlURL =
				 						DLUtil.getPreviewURL(
				 							fileEntry, fileVersion, themeDisplay,
				 							queryString, appendFileEntryVersion,
				 							useAbsoluteURL);
							 	}
							 %>
							<c:choose>
								<c:when test="<%= dlURL != null %>">
									<a target="_blank" href="<%= dlURL %>">
										<liferay-ui:message key="view-confirm-file-entry"/>
									</a>
								</c:when>
								<c:otherwise>
									-
								</c:otherwise>
							</c:choose>
						</p>
					</div>
					
					<div>
						<aui:input type="radio" onChange="paymentFormConfirm('1');" name="confirmHopLe" value="1" label="hop-le" inlineField="true"></aui:input>
						<aui:input type="radio" onChange="paymentFormConfirm('0');" name="confirmHopLe" value="0" label="khong-hop-le" inlineField="true"></aui:input>
						<aui:input type="hidden" name="confirmHopLeHidden" value="0" />
						<aui:input type="text" cssClass="input100" name="lyDo" placeholder="noi-dung-xac-nhan" label=""></aui:input>
						<aui:button name="register" type="button" value="dong-y" disabled="true" />
					</div>
				</div>
				</aui:form>
			</div>
		</div>
		
		<script type="text/javascript">
			var A = AUI();
			function paymentFormConfirm(code) {
				var register = A.one('#<portlet:namespace />register');
				var confirmHopLeHidden = A
						.one('#<portlet:namespace />confirmHopLeHidden');
				confirmHopLeHidden.val(code);
				register.removeClass('disabled');
				register.removeAttribute('disabled');
				register.setAttribute('onClick', 'paymentFormSubmit();');
			}
			function paymentFormSubmit() {
				var lyDo = A.one("#<portlet:namespace />lyDo");
				var confirmHopLeHidden = AUI().one(
						"#<portlet:namespace />confirmHopLeHidden");
				if (confirmHopLeHidden.val() == 0 && lyDo.val() == '') {
					alert('<liferay-ui:message key="khong-hop-le-yeu-cau-nhap-ro-ly-do"/>');
				} else {
					document.getElementById('<portlet:namespace />payForm').submit();
				}
			}
		</script>
	</c:when>
	
	<c:otherwise>
		<div class="alert">
			<strong>
				<liferay-ui:message key="warning">
				!
				</liferay-ui:message>
			</strong>
			&nbsp;
			<liferay-ui:message key="working-unit-not-found"/>
		</div>
	</c:otherwise>
</c:choose>

<style>
.label.opencps.dossiermgt.part-file-ctr{
	color: #fff !important;
	background-color: #25BB61;
	float: left;
	margin-left: 5px;
	padding: 5px 3px;
}
</style>