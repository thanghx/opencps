
package org.opencps.accountmgt.portlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.opencps.accountmgt.DuplicateBusinessEmailException;
import org.opencps.accountmgt.DuplicateCitizenEmailException;
import org.opencps.accountmgt.OutOfLengthBusinessEmailException;
import org.opencps.accountmgt.OutOfLengthBusinessEnNameException;
import org.opencps.accountmgt.OutOfLengthBusinessNameException;
import org.opencps.accountmgt.OutOfLengthBusinessRepresentativeNameException;
import org.opencps.accountmgt.OutOfLengthBusinessRepresentativeRoleException;
import org.opencps.accountmgt.OutOfLengthBusinessShortNameException;
import org.opencps.accountmgt.OutOfLengthCitizenAddressException;
import org.opencps.accountmgt.OutOfLengthCitizenEmailException;
import org.opencps.accountmgt.OutOfLengthCitizenNameException;
import org.opencps.accountmgt.model.Business;
import org.opencps.accountmgt.model.Citizen;
import org.opencps.accountmgt.search.BusinessDisplayTerms;
import org.opencps.accountmgt.search.CitizenDisplayTerms;
import org.opencps.accountmgt.service.BusinessLocalServiceUtil;
import org.opencps.accountmgt.service.CitizenLocalServiceUtil;
import org.opencps.datamgt.model.DictItem;
import org.opencps.datamgt.service.DictItemLocalServiceUtil;
import org.opencps.util.DateTimeUtil;
import org.opencps.util.MessageBusUtil;
import org.opencps.util.PortletUtil;
import org.opencps.util.WebKeys;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.MimeTypesUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.model.User;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.ServiceContextFactory;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;

public class AccountRegPortlet extends MVCPortlet {

	@Override
	public void render(
	    RenderRequest renderRequest, RenderResponse renderResponse)
	    throws PortletException, IOException {

		long citizenId = ParamUtil
		    .getLong(renderRequest, CitizenDisplayTerms.CITIZEN_ID);

		long businessId = ParamUtil
		    .getLong(renderRequest, BusinessDisplayTerms.BUSINESS_BUSINESSID);

		try {
			if (citizenId > 0) {
				Citizen citizen = CitizenLocalServiceUtil
				    .fetchCitizen(citizenId);
				renderRequest
				    .setAttribute(WebKeys.CITIZEN_ENTRY, citizen);
			}

			if (businessId > 0) {
				Business business = BusinessLocalServiceUtil
				    .fetchBusiness(businessId);
				renderRequest
				    .setAttribute(WebKeys.BUSINESS_ENTRY, business);
			}
		}

		catch (Exception e) {
			_log
			    .error(e);
		}

		super.render(renderRequest, renderResponse);
	}

	public void updateBusiness(
	    ActionRequest actionRequest, ActionResponse actionResponse) {

		UploadPortletRequest uploadPortletRequest = PortalUtil
		    .getUploadPortletRequest(actionRequest);

		long businessId = ParamUtil
		    .getLong(
		        uploadPortletRequest, BusinessDisplayTerms.BUSINESS_BUSINESSID);

		long cityId = ParamUtil
		    .getLong(
		        uploadPortletRequest, BusinessDisplayTerms.BUSINESS_CITY_ID);
		long districtId = ParamUtil
		    .getLong(
		        uploadPortletRequest,
		        BusinessDisplayTerms.BUSINESS_DISTRICT_ID);
		long wardId = ParamUtil
		    .getLong(
		        uploadPortletRequest, BusinessDisplayTerms.BUSINESS_WARD_ID);

		long repositoryId = 0;

		long size = uploadPortletRequest
		    .getSize(BusinessDisplayTerms.BUSINESS_ATTACHFILE);

		String name = ParamUtil
		    .getString(
		        uploadPortletRequest, BusinessDisplayTerms.BUSINESS_NAME);
		String enName = ParamUtil
		    .getString(
		        uploadPortletRequest, BusinessDisplayTerms.BUSINESS_ENNAME);
		String idNumber = ParamUtil
		    .getString(
		        uploadPortletRequest, BusinessDisplayTerms.BUSINESS_IDNUMBER);
		String shortName = ParamUtil
		    .getString(
		        uploadPortletRequest, BusinessDisplayTerms.BUSINESS_SHORTNAME);
		String type = ParamUtil
		    .getString(
		        uploadPortletRequest,
		        BusinessDisplayTerms.BUSINESS_BUSINESSTYPE);
		String address = ParamUtil
		    .getString(
		        uploadPortletRequest, BusinessDisplayTerms.BUSINESS_ADDRESS);
		String email = ParamUtil
		    .getString(
		        uploadPortletRequest, BusinessDisplayTerms.BUSINESS_EMAIL);
		String telNo = ParamUtil
		    .getString(
		        uploadPortletRequest, BusinessDisplayTerms.BUSINESS_TELNO);
		String representativeName = ParamUtil
		    .getString(
		        uploadPortletRequest,
		        BusinessDisplayTerms.BUSINESS_REPRESENTATIVENAME);
		String representativeRole = ParamUtil
		    .getString(
		        uploadPortletRequest,
		        BusinessDisplayTerms.BUSINESS_REPRESENTATIVEROLE);

		String contentType = uploadPortletRequest
		    .getContentType(BusinessDisplayTerms.BUSINESS_ATTACHFILE);

		String sourceFileName = uploadPortletRequest
		    .getFileName(BusinessDisplayTerms.BUSINESS_ATTACHFILE);

		String[] domain = ParamUtil
		    .getParameterValues(
		        uploadPortletRequest, BusinessDisplayTerms.BUSINESS_DOMAIN);

		Date defaultBirthDate = DateTimeUtil
		    .convertStringToDate("01/01/1970");

		PortletUtil.SplitDate spd = new PortletUtil.SplitDate(defaultBirthDate);

		contentType = MimeTypesUtil
		    .getContentType(contentType);

		String title = "Business File";

		DictItem city = null;

		DictItem district = null;

		DictItem ward = null;

		InputStream inputStream = null;

		try {
			ValidateBusiness(
			    businessId, email, sourceFileName, enName, shortName, address,
			    representativeName, representativeRole);
			ServiceContext serviceContext = ServiceContextFactory
			    .getInstance(uploadPortletRequest);

			inputStream = uploadPortletRequest
			    .getFileAsStream(CitizenDisplayTerms.CITIZEN_ATTACHFILE);

			repositoryId = serviceContext
			    .getScopeGroupId();
			city = DictItemLocalServiceUtil
			    .getDictItem(cityId);
			district = DictItemLocalServiceUtil
			    .getDictItem(districtId);
			ward = DictItemLocalServiceUtil
			    .getDictItem(wardId);

			if (businessId == 0) {

				BusinessLocalServiceUtil
				    .addBusiness(
				        name, enName, shortName, type, idNumber, address, city
				            .getItemCode(),
				        district
				            .getItemCode(),
				        ward
				            .getItemCode(),
				        city
				            .getItemName(serviceContext
				                .getLocale(), true),
				        district
				            .getItemName(serviceContext
				                .getLocale(), true),
				        ward
				            .getItemName(serviceContext
				                .getLocale(), true),
				        telNo, email, representativeName, representativeRole,
				        domain, spd
				            .getDayOfMoth(),
				        spd
				            .getMonth(),
				        spd
				            .getYear(),
				        repositoryId, sourceFileName, contentType, title,
				        inputStream, size, serviceContext);
			}
			else {

			}
		}

		catch (Exception e) {
			if (e instanceof DuplicateBusinessEmailException) {
				SessionErrors
				    .add(actionRequest, DuplicateBusinessEmailException.class);
			}
			else if (e instanceof OutOfLengthBusinessEmailException) {
				SessionErrors
				    .add(
				        actionRequest, OutOfLengthBusinessEmailException.class);
			}
			else if (e instanceof OutOfLengthBusinessNameException) {
				SessionErrors
				    .add(actionRequest, OutOfLengthBusinessNameException.class);
			}
			else if (e instanceof OutOfLengthBusinessEnNameException) {
				SessionErrors
				    .add(
				        actionRequest,
				        OutOfLengthBusinessEnNameException.class);
			}
			else if (e instanceof OutOfLengthBusinessShortNameException) {
				SessionErrors
				    .add(
				        actionRequest,
				        OutOfLengthBusinessShortNameException.class);
			}
			else if (e instanceof OutOfLengthBusinessRepresentativeNameException) {
				SessionErrors
				    .add(
				        actionRequest,
				        OutOfLengthBusinessRepresentativeNameException.class);
			}
			else if (e instanceof OutOfLengthBusinessRepresentativeRoleException) {
				SessionErrors
				    .add(
				        actionRequest,
				        OutOfLengthBusinessRepresentativeRoleException.class);
			}
			else if (e instanceof OutOfLengthCitizenAddressException) {
				SessionErrors
				    .add(
				        actionRequest,
				        OutOfLengthCitizenAddressException.class);
			}

		}
		finally {

		}

	}

	public void updateCitizen(
	    ActionRequest actionRequest, ActionResponse actionResponse) {

		UploadPortletRequest uploadPortletRequest = PortalUtil
		    .getUploadPortletRequest(actionRequest);

		long citizenId = ParamUtil
		    .getLong(uploadPortletRequest, CitizenDisplayTerms.CITIZEN_ID);

		long cityId = ParamUtil
		    .getLong(uploadPortletRequest, CitizenDisplayTerms.CITIZEN_CITY_ID);
		long districtId = ParamUtil
		    .getLong(
		        uploadPortletRequest, CitizenDisplayTerms.CITIZEN_DISTRICT_ID);
		long wardId = ParamUtil
		    .getLong(uploadPortletRequest, CitizenDisplayTerms.CITIZEN_WARD_ID);

		long size = uploadPortletRequest
		    .getSize(CitizenDisplayTerms.CITIZEN_ATTACHFILE);

		long repositoryId = 0;

		String fullName = ParamUtil
		    .getString(
		        uploadPortletRequest, CitizenDisplayTerms.CITIZEN_FULLNAME);
		String personId = ParamUtil
		    .getString(
		        uploadPortletRequest, CitizenDisplayTerms.CITIZEN_PERSONALID);
		String adress = ParamUtil
		    .getString(
		        uploadPortletRequest, CitizenDisplayTerms.CITIZEN_ADDRESS);
		String email = ParamUtil
		    .getString(uploadPortletRequest, CitizenDisplayTerms.CITIZEN_EMAIL);
		String telNo = ParamUtil
		    .getString(uploadPortletRequest, CitizenDisplayTerms.CITIZEN_TELNO);

		String contentType = uploadPortletRequest
		    .getContentType(CitizenDisplayTerms.CITIZEN_ATTACHFILE);

		String sourceFileName = uploadPortletRequest
		    .getFileName(CitizenDisplayTerms.CITIZEN_ATTACHFILE);

		int birthDateDay = ParamUtil
		    .getInteger(
		        uploadPortletRequest, CitizenDisplayTerms.BIRTH_DATE_DAY);
		int birthDateMonth = ParamUtil
		    .getInteger(
		        uploadPortletRequest, CitizenDisplayTerms.BIRTH_DATE_MONTH);
		int birthDateYear = ParamUtil
		    .getInteger(
		        uploadPortletRequest, CitizenDisplayTerms.BIRTH_DATE_YEAR);
		int gender = ParamUtil
		    .getInteger(
		        uploadPortletRequest, CitizenDisplayTerms.CITIZEN_GENDER);

		contentType = MimeTypesUtil
		    .getContentType(contentType);

		String title = "Personal File";

		DictItem city = null;

		DictItem district = null;

		DictItem ward = null;

		InputStream inputStream = null;

		try {

			ValidateCitizen(
			    citizenId, fullName, personId, adress, email, telNo, size,
			    contentType);

			ServiceContext serviceContext = ServiceContextFactory
			    .getInstance(actionRequest);

			inputStream = uploadPortletRequest
			    .getFileAsStream(CitizenDisplayTerms.CITIZEN_ATTACHFILE);

			repositoryId = serviceContext
			    .getScopeGroupId();

			city = DictItemLocalServiceUtil
			    .getDictItem(cityId);

			district = DictItemLocalServiceUtil
			    .getDictItem(districtId);

			ward = DictItemLocalServiceUtil
			    .getDictItem(wardId);

			if (citizenId == 0) {
				Citizen citizen = CitizenLocalServiceUtil
				    .addCitizen(
				        fullName, personId, gender, birthDateDay,
				        birthDateMonth, birthDateYear, adress, city
				            .getItemCode(),
				        district
				            .getItemCode(),
				        ward
				            .getItemCode(),
				        city
				            .getItemName(serviceContext
				                .getLocale(), true),
				        district
				            .getItemName(serviceContext
				                .getLocale(), true),
				        ward
				            .getItemName(serviceContext
				                .getLocale(), true),
				        email,

				        telNo, repositoryId, sourceFileName, contentType, title,
				        inputStream, size, serviceContext);

				if (citizen != null) {
					User mappingUser = UserLocalServiceUtil
					    .getUser(citizen
					        .getMappingUserId());
					MessageBusUtil
					    .sendEmailAddressVerification(
					        mappingUser, email, serviceContext);
				}

			}
			else {

			}

		}
		catch (Exception e) {
			if (e instanceof OutOfLengthCitizenAddressException) {
				SessionErrors
				    .add(
				        actionRequest,
				        OutOfLengthCitizenAddressException.class);
			}
			else if (e instanceof OutOfLengthCitizenEmailException) {
				SessionErrors
				    .add(actionRequest, OutOfLengthCitizenEmailException.class);
			}
			else if (e instanceof OutOfLengthCitizenNameException) {
				SessionErrors
				    .add(actionRequest, OutOfLengthCitizenNameException.class);
			}
			else if (e instanceof DuplicateCitizenEmailException) {
				SessionErrors
				    .add(actionRequest, DuplicateCitizenEmailException.class);
			}else{
				
			}
			_log.error(e);	
		}
		finally {

		}
	}

	protected void ValidateCitizen(
	    long citizenId, String fullName, String personalId, String address,
	    String email, String telNo, long size, String mimeType)
	    throws OutOfLengthCitizenAddressException,
	    OutOfLengthCitizenNameException, OutOfLengthCitizenEmailException,
	    DuplicateCitizenEmailException {

		Citizen citizen = null;

		try {
			citizen = CitizenLocalServiceUtil
			    .getCitizen(email);
		}
		catch (Exception e) {
			// Nothing todo
		}
		if (citizenId == 0 && citizen != null) {
			throw new DuplicateCitizenEmailException();
		}
		if (citizenId > 0 && citizen
		    .getCitizenId() != citizenId) {
			throw new DuplicateCitizenEmailException();
		}

		if (fullName
		    .length() > 255) {
			throw new OutOfLengthCitizenNameException();
		}
		else if (address
		    .length() > 500) {
			throw new OutOfLengthCitizenAddressException();
		}
		else if (email
		    .length() > 255) {
			throw new OutOfLengthCitizenEmailException();
		}

	}

	protected void ValidateBusiness(
	    long businessId, String email, String name, String enName,
	    String shortName, String address, String representativeName,
	    String representativeRole)
	    throws DuplicateBusinessEmailException,
	    OutOfLengthBusinessEmailException, OutOfLengthBusinessNameException,
	    OutOfLengthBusinessEnNameException, OutOfLengthCitizenAddressException,
	    OutOfLengthBusinessRepresentativeNameException,
	    OutOfLengthBusinessRepresentativeRoleException,
	    OutOfLengthBusinessShortNameException {

		Business business = null;

		try {
			business = BusinessLocalServiceUtil
			    .getBusiness(email);
		}
		catch (Exception e) {
			_log
			    .error(e);
		}

		if (businessId == 0 && business != null) {
			throw new DuplicateBusinessEmailException();
		}

		if (businessId != 0 && business != null && business
		    .getBusinessId() != businessId) {
			throw new DuplicateBusinessEmailException();
		}
		if (email
		    .length() > 255) {
			throw new OutOfLengthBusinessEmailException();
		}
		else if (name
		    .length() > 255) {
			throw new OutOfLengthBusinessNameException();
		}
		else if (enName
		    .length() > 255) {
			throw new OutOfLengthBusinessEnNameException();
		}
		else if (address
		    .length() > 500) {
			throw new OutOfLengthCitizenAddressException();
		}
		else if (representativeName
		    .length() > 75) {
			throw new OutOfLengthBusinessRepresentativeNameException();
		}
		else if (representativeRole
		    .length() > 75) {
			throw new OutOfLengthBusinessRepresentativeRoleException();
		}
		else if (shortName
		    .length() > 75) {
			throw new OutOfLengthBusinessShortNameException();
		}

	}

	private Log _log = LogFactoryUtil
	    .getLog(AccountRegPortlet.class
	        .getName());
}
