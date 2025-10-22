package com.polarisoffice.security.dto;

import java.time.LocalDate;

public class ServiceUnlessDto {
    @Override
	public String toString() {
		return "ServiceUnlessDto [serviceId=" + serviceId + ", serviceName=" + serviceName + ", customerName="
				+ customerName + ", contactName=" + contactName + ", createAt=" + createAt + ", customerId="
				+ customerId + "]";
	}

	private Integer serviceId;
    private String serviceName;
    private String customerName;
    private String contactName;
    private LocalDate createAt;
    private String customerId;  // customerId 필드 추가

    // Constructor, Getter, Setter

    public ServiceUnlessDto(Integer serviceId, String serviceName, String customerName,
                            String contactName, LocalDate createAt, String customerId) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.customerName = customerName;
        this.contactName = contactName;
        this.createAt = createAt;
        this.customerId = customerId;
    }

    // Getter and Setter for customerId
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

	public Integer getServiceId() {
		return serviceId;
	}

	public void setServiceId(Integer serviceId) {
		this.serviceId = serviceId;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public LocalDate getCreateAt() {
		return createAt;
	}

	public void setCreateAt(LocalDate createAt) {
		this.createAt = createAt;
	}

}

