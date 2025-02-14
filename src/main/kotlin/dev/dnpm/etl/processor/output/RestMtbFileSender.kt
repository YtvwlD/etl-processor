/*
 * This file is part of ETL-Processor
 *
 * Copyright (c) 2023  Comprehensive Cancer Center Mainfranken, Datenintegrationszentrum Philipps-Universität Marburg and Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.dnpm.etl.processor.output

import dev.dnpm.etl.processor.config.RestTargetProperties
import dev.dnpm.etl.processor.monitoring.RequestStatus
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

class RestMtbFileSender(
    private val restTemplate: RestTemplate,
    private val restTargetProperties: RestTargetProperties
) : MtbFileSender {

    private val logger = LoggerFactory.getLogger(RestMtbFileSender::class.java)

    override fun send(request: MtbFileSender.MtbFileRequest): MtbFileSender.Response {
        try {
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            val entityReq = HttpEntity(request.mtbFile, headers)
            val response = restTemplate.postForEntity(
                "${restTargetProperties.uri}/MTBFile",
                entityReq,
                String::class.java
            )
            if (!response.statusCode.is2xxSuccessful) {
                logger.warn("Error sending to remote system: {}", response.body)
                return MtbFileSender.Response(response.statusCode.asRequestStatus(), "Status-Code: ${response.statusCode.value()}")
            }
            logger.debug("Sent file via RestMtbFileSender")
            return MtbFileSender.Response(response.statusCode.asRequestStatus(), response.body.orEmpty())
        } catch (e: IllegalArgumentException) {
            logger.error("Not a valid URI to export to: '{}'", restTargetProperties.uri!!)
        } catch (e: RestClientException) {
            logger.info(restTargetProperties.uri!!.toString())
            logger.error("Cannot send data to remote system", e)
        }
        return MtbFileSender.Response(RequestStatus.ERROR, "Sonstiger Fehler bei der Übertragung")
    }

    override fun send(request: MtbFileSender.DeleteRequest): MtbFileSender.Response {
        try {
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            val entityReq = HttpEntity(null, headers)
            restTemplate.delete(
                "${restTargetProperties.uri}/Patient/${request.patientId}",
                entityReq,
                String::class.java
            )
            logger.debug("Sent file via RestMtbFileSender")
            return MtbFileSender.Response(RequestStatus.SUCCESS)
        } catch (e: IllegalArgumentException) {
            logger.error("Not a valid URI to export to: '{}'", restTargetProperties.uri!!)
        } catch (e: RestClientException) {
            logger.info(restTargetProperties.uri!!.toString())
            logger.error("Cannot send data to remote system", e)
        }
        return MtbFileSender.Response(RequestStatus.ERROR, "Sonstiger Fehler bei der Übertragung")
    }

}