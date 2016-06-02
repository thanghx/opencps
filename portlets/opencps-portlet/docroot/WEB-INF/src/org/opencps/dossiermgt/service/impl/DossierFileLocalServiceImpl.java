/*******************************************************************************
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
 *******************************************************************************/
package org.opencps.dossiermgt.service.impl;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.opencps.dossiermgt.NoSuchDossierFileException;
import org.opencps.dossiermgt.model.DossierFile;
import org.opencps.dossiermgt.model.FileGroup;
import org.opencps.dossiermgt.service.DossierFileLocalServiceUtil;
import org.opencps.dossiermgt.service.base.DossierFileLocalServiceBaseImpl;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.uuid.PortalUUIDUtil;
import com.liferay.portal.service.ServiceContext;

/**
 * The implementation of the dossier file local service. <p> All custom service
 * methods should be put in this class. Whenever methods are added, rerun
 * ServiceBuilder to copy their definitions into the
 * {@link org.opencps.dossiermgt.service.DossierFileLocalService} interface. <p>
 * This is a local service. Methods of this service will not have security
 * checks based on the propagated JAAS credentials because this service can only
 * be accessed from within the same VM. </p>
 *
 * @author trungnt
 * @see org.opencps.dossiermgt.service.base.DossierFileLocalServiceBaseImpl
 * @see org.opencps.dossiermgt.service.DossierFileLocalServiceUtil
 */

/**
 * @author trungnt
 *
 */
public class DossierFileLocalServiceImpl
	extends DossierFileLocalServiceBaseImpl {
	/*
	 * NOTE FOR DEVELOPERS: Never reference this interface directly. Always use
	 * {@link org.opencps.dossiermgt.service.DossierFileLocalServiceUtil} to
	 * access the dossier file local service.
	 */

	/*
	 * Removed getDossierFileByDossierAndDossierPart
	 */

	
	/**
	 * @param userId
	 * @param dossierId
	 * @param dossierPartId
	 * @param templateFileNo
	 * @param groupName
	 * @param fileGroupId
	 * @param ownerUserId
	 * @param ownerOrganizationId
	 * @param displayName
	 * @param formData
	 * @param fileEntryId
	 * @param dossierFileMark
	 * @param dossierFileType
	 * @param dossierFileNo
	 * @param dossierFileDate
	 * @param original
	 * @param syncStatus
	 * @param version
	 * @param serviceContext
	 * @return
	 * @throws SystemException
	 * @throws PortalException
	 */
	public DossierFile addDossierFile(
		long userId, long dossierId, long dossierPartId, String templateFileNo,
		String groupName, long fileGroupId, long ownerUserId,
		long ownerOrganizationId, String displayName, String formData,
		long fileEntryId, int dossierFileMark, int dossierFileType,
		String dossierFileNo, Date dossierFileDate, int original,
		int syncStatus, int version, ServiceContext serviceContext)
		throws SystemException, PortalException {

		long dossierFileId = counterLocalService
			.increment(DossierFile.class
				.getName());
		DossierFile dossierFile = dossierFilePersistence
			.create(dossierFileId);

		Date now = new Date();

		// Add new FileGroup
		if (Validator
			.isNotNull(groupName) && fileGroupId == 0) {
			FileGroup fileGroup = fileGroupLocalService
				.addFileGroup(ownerUserId, dossierId, dossierPartId, groupName,
					syncStatus, serviceContext);

			fileGroupId = fileGroup
				.getFileGroupId();
		}

		dossierFile
			.setUserId(userId);
		dossierFile
			.setGroupId(serviceContext
				.getScopeGroupId());
		dossierFile
			.setCompanyId(serviceContext
				.getCompanyId());
		dossierFile
			.setCreateDate(now);
		dossierFile
			.setModifiedDate(now);
		dossierFile
			.setDisplayName(displayName);
		dossierFile
			.setDossierFileDate(dossierFileDate);
		dossierFile
			.setDossierFileMark(dossierFileMark);
		dossierFile
			.setDossierFileNo(dossierFileNo);
		dossierFile
			.setDossierFileType(dossierFileType);
		dossierFile
			.setDossierId(dossierId);
		dossierFile
			.setDossierPartId(dossierPartId);
		dossierFile
			.setFileEntryId(fileEntryId);
		dossierFile
			.setFormData(formData);
		dossierFile
			.setGroupFileId(fileGroupId);
		dossierFile
			.setOriginal(original);
		dossierFile
			.setOwnerUserId(ownerUserId);

		dossierFile
			.setOwnerOrganizationId(ownerOrganizationId);

		dossierFile
			.setVersion(version);
		DossierFile curVersion = null;

		try {
			curVersion = dossierFileLocalService
				.getDossierFileInUse(dossierId, dossierPartId);
		}
		catch (Exception e) {
		}

		if (curVersion != null) {
			dossierFile
				.setOid(curVersion
					.getOid());
			dossierFileLocalService
				.removeDossierFile(curVersion
					.getDossierFileId());
		}
		else {
			dossierFile
				.setOid(PortalUUIDUtil
					.generate());
		}

		dossierFile = dossierFilePersistence
			.update(dossierFile);
		Indexer indexer = IndexerRegistryUtil
			.nullSafeGetIndexer(DossierFile.class);

		indexer
			.reindex(dossierFile);

		return dossierFile;
	}
	
	/**
	 * @param userId
	 * @param dossierId
	 * @param dossierPartId
	 * @param templateFileNo
	 * @param groupName
	 * @param fileGroupId
	 * @param ownerUserId
	 * @param ownerOrganizationId
	 * @param displayName
	 * @param formData
	 * @param fileEntryId
	 * @param dossierFileMark
	 * @param dossierFileType
	 * @param dossierFileNo
	 * @param dossierFileDate
	 * @param original
	 * @param syncStatus
	 * @param version
	 * @param folderId
	 * @param sourceFileName
	 * @param mimeType
	 * @param title
	 * @param description
	 * @param changeLog
	 * @param is
	 * @param size
	 * @param serviceContext
	 * @return
	 * @throws SystemException
	 * @throws PortalException
	 */
	public DossierFile addDossierFile(
		long userId, long dossierId, long dossierPartId, String templateFileNo,
		String groupName, long fileGroupId, long ownerUserId,
		long ownerOrganizationId, String displayName, String formData,
		long fileEntryId, int dossierFileMark, int dossierFileType,
		String dossierFileNo, Date dossierFileDate, int original,
		int syncStatus, int version, long folderId, String sourceFileName,
		String mimeType, String title, String description, String changeLog,
		InputStream is, long size, ServiceContext serviceContext)
		throws SystemException, PortalException {

		long dossierFileId = counterLocalService
			.increment(DossierFile.class
				.getName());
		DossierFile dossierFile = dossierFilePersistence
			.create(dossierFileId);

		Date now = new Date();

		// Add new FileGroup
		if (Validator
			.isNotNull(groupName) && fileGroupId == 0) {
			FileGroup fileGroup = fileGroupLocalService
				.addFileGroup(ownerUserId, dossierId, dossierPartId, groupName,
					syncStatus, serviceContext);

			fileGroupId = fileGroup
				.getFileGroupId();
		}

		// Add file
		FileEntry fileEntry = dlAppService
			.addFileEntry(serviceContext
				.getScopeGroupId(), folderId, sourceFileName, mimeType,
				displayName, description, changeLog, is, size, serviceContext);

		dossierFile
			.setUserId(userId);
		dossierFile
			.setGroupId(serviceContext
				.getScopeGroupId());
		dossierFile
			.setCompanyId(serviceContext
				.getCompanyId());
		dossierFile
			.setCreateDate(now);
		dossierFile
			.setModifiedDate(now);
		dossierFile
			.setDisplayName(displayName);
		dossierFile
			.setDossierFileDate(dossierFileDate);
		dossierFile
			.setDossierFileMark(dossierFileMark);
		dossierFile
			.setDossierFileNo(dossierFileNo);
		dossierFile
			.setDossierFileType(dossierFileType);
		dossierFile
			.setDossierId(dossierId);
		dossierFile
			.setDossierPartId(dossierPartId);
		dossierFile
			.setFileEntryId(fileEntry
				.getFileEntryId());
		dossierFile
			.setFormData(formData);
		dossierFile
			.setGroupFileId(fileGroupId);
		dossierFile
			.setOriginal(original);
		dossierFile
			.setOwnerUserId(ownerUserId);

		dossierFile
			.setOwnerOrganizationId(ownerOrganizationId);

		dossierFile
			.setVersion(version);
		DossierFile curVersion = null;

		try {
			curVersion = dossierFileLocalService
				.getDossierFileInUse(dossierId, dossierPartId);
		}
		catch (Exception e) {
		}

		if (curVersion != null) {
			dossierFile
				.setOid(curVersion
					.getOid());
			dossierFileLocalService
				.removeDossierFile(curVersion
					.getDossierFileId());
		}
		else {
			dossierFile
				.setOid(PortalUUIDUtil
					.generate());
		}

		dossierFile = dossierFilePersistence
			.update(dossierFile);
		Indexer indexer = IndexerRegistryUtil
			.nullSafeGetIndexer(DossierFile.class);

		indexer
			.reindex(dossierFile);

		return dossierFile;
	}

	/**
	 * @param dossierId
	 * @param dossierPartId
	 * @return
	 * @throws SystemException
	 */
	public int countDossierFile(long dossierId, long dossierPartId)
		throws SystemException {

		return dossierFilePersistence
			.countByD_DP(dossierId, dossierPartId);
	}

	/**
	 * @param groupId
	 * @param keyword
	 * @param dossierTemplateId
	 * @param fileEntryId
	 * @param onlyViewFileResult
	 * @return
	 * @throws SystemException
	 */
	public int countDossierFile(
		long groupId, String keyword, long dossierTemplateId, long fileEntryId,
		boolean onlyViewFileResult)
		throws SystemException {

		return dossierFileFinder
			.countDossierFile(groupId, keyword, dossierTemplateId, fileEntryId,
				onlyViewFileResult);
	}
	
	/**
	 * @param dossierFileId
	 * @throws PortalException
	 * @throws SystemException
	 */
	public void removeDossierFile(long dossierFileId)
		throws PortalException, SystemException {

		Indexer indexer = IndexerRegistryUtil
			.nullSafeGetIndexer(DossierFile.class);

		DossierFile dossierFile = DossierFileLocalServiceUtil
			.getDossierFile(dossierFileId);

		dossierFile
			.setRemoved(1);
		dossierFile
			.setModifiedDate(new Date());

		indexer
			.reindex(dossierFile);

		dossierFilePersistence
			.remove(dossierFileId);
	}

	/**
	 * @param dossierFileId
	 * @param fileEntryId
	 * @throws PortalException
	 * @throws SystemException
	 */
	public void deleteDossierFile(long dossierFileId, long fileEntryId)
		throws PortalException, SystemException {

		if (fileEntryId > 0) {
			dlFileEntryLocalService
				.deleteDLFileEntry(fileEntryId);
		}

		Indexer indexer = IndexerRegistryUtil
			.nullSafeGetIndexer(DossierFile.class);

		DossierFile dossierFile = DossierFileLocalServiceUtil
			.getDossierFile(dossierFileId);
		indexer
			.delete(dossierFile);

		dossierFilePersistence
			.remove(dossierFileId);
	}

	/**
	 * @param dossierId
	 * @param dossierPartId
	 * @return List
	 * @throws NoSuchDossierFileException
	 * @throws SystemException
	 */
	public List<DossierFile> getDossierFileByD_DP(
		long dossierId, long dossierPartId)
		throws NoSuchDossierFileException, SystemException {

		return dossierFilePersistence
			.findByD_DP(dossierId, dossierPartId);
	}

	/**
	 * Find all dossierFile in use(removed = 0)
	 * 
	 * @param dossierId
	 * @param groupFileId
	 * @return
	 * @throws SystemException
	 */
	public List<DossierFile> getDossierFileByD_GF(
		long dossierId, long groupFileId)
		throws SystemException {

		return dossierFilePersistence
			.findByD_GF(dossierId, groupFileId);
	}

	/**
	 * Find all dossierFile in use (removed = 0)
	 * 
	 * @param dossierId
	 * @return List<DossierFile>
	 * @throws NoSuchDossierFileException
	 * @throws SystemException
	 */
	public List<DossierFile> getDossierFileByDossierId(long dossierId)
		throws SystemException {

		return dossierFilePersistence
			.findByDossierId(dossierId);
	}

	/**
	 * @param dossierId
	 * @param dossierPartId
	 * @return DossierFile
	 * @throws NoSuchDossierFileException
	 * @throws SystemException
	 */
	public DossierFile getDossierFileInUse(long dossierId, long dossierPartId)
		throws NoSuchDossierFileException, SystemException {

		return dossierFilePersistence
			.findByDossierFileInUse(dossierId, dossierPartId);
	}

	/**
	 * @param groupId
	 * @param keyword
	 * @param dossierTemplateId
	 * @param fileEntryId
	 * @param onlyViewFileResult
	 * @param start
	 * @param end
	 * @param obc
	 * @return
	 * @throws SystemException
	 */
	public List<DossierFile> searchDossierFile(
		long groupId, String keyword, long dossierTemplateId, long fileEntryId,
		boolean onlyViewFileResult, int start, int end, OrderByComparator obc)
		throws SystemException {

		return dossierFileFinder
			.searchDossierFile(groupId, keyword, dossierTemplateId, fileEntryId,
				onlyViewFileResult, start, end, obc);
	}

	/**
	 * @param dossierFileId
	 * @param ownerUserId
	 * @param ownerOrganizationId
	 * @param fileEntryId
	 * @param displayName
	 * @return
	 * @throws NoSuchDossierFileException
	 * @throws SystemException
	 * @throws PortalException
	 */
	public DossierFile updateDossierFile(
		long dossierFileId, long ownerUserId, long ownerOrganizationId,
		long fileEntryId, String displayName)
		throws NoSuchDossierFileException, SystemException, PortalException {

		DossierFile dossierFile = dossierFilePersistence
			.findByPrimaryKey(dossierFileId);

		Date now = new Date();

		dossierFile
			.setModifiedDate(now);

		dossierFile
			.setDisplayName(displayName);

		dossierFile
			.setFileEntryId(fileEntryId);
		dossierFile
			.setOwnerUserId(ownerUserId);
		dossierFile
			.setOwnerOrganizationId(ownerOrganizationId);
		dossierFile = dossierFilePersistence
			.update(dossierFile);
		Indexer indexer = IndexerRegistryUtil
			.nullSafeGetIndexer(DossierFile.class);

		indexer
			.reindex(dossierFile);

		return dossierFile;
	}

	/*
	 * public List<DossierFile> getDossierFileByDossierAndDossierPart( long
	 * dossierId, long dossierPartId) throws SystemException { return
	 * dossierFilePersistence .findByD_P_C(dossierId, dossierPartId); }
	 */

	/**
	 * @param dossierFileId
	 * @param userId
	 * @param dossierId
	 * @param dossierPartId
	 * @param templateFileNo
	 * @param groupFileId
	 * @param ownerUserId
	 * @param ownerOrganizationId
	 * @param displayName
	 * @param formData
	 * @param fileEntryId
	 * @param dossierFileMark
	 * @param dossierFileType
	 * @param dossierFileNo
	 * @param dossierFileDate
	 * @param original
	 * @param syncStatus
	 * @param serviceContext
	 * @return
	 * @throws NoSuchDossierFileException
	 * @throws SystemException
	 */
	public DossierFile updateDossierFile(
		long dossierFileId, long userId, long dossierId, long dossierPartId,
		String templateFileNo, long groupFileId, long ownerUserId,
		long ownerOrganizationId, String displayName, String formData,
		long fileEntryId, int dossierFileMark, int dossierFileType,
		String dossierFileNo, Date dossierFileDate, int original,
		int syncStatus, ServiceContext serviceContext)
		throws NoSuchDossierFileException, SystemException {

		DossierFile dossierFile = dossierFilePersistence
			.findByPrimaryKey(dossierFileId);

		Date now = new Date();

		dossierFile
			.setModifiedDate(now);
		dossierFile
			.setUserId(userId);
		dossierFile
			.setDisplayName(displayName);
		dossierFile
			.setDossierFileDate(dossierFileDate);
		dossierFile
			.setDossierFileMark(dossierFileMark);
		dossierFile
			.setDossierFileNo(dossierFileNo);
		dossierFile
			.setDossierFileType(dossierFileType);
		dossierFile
			.setDossierId(dossierId);
		dossierFile
			.setDossierPartId(dossierPartId);
		dossierFile
			.setFileEntryId(fileEntryId);
		dossierFile
			.setFormData(formData);
		dossierFile
			.setGroupFileId(groupFileId);
		dossierFile
			.setOriginal(original);
		dossierFile
			.setOwnerOrganizationId(ownerOrganizationId);
		return dossierFilePersistence
			.update(dossierFile);
	}
}
