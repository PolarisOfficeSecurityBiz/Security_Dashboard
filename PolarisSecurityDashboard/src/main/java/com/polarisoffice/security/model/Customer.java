package com.polarisoffice.security.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Customer {

    @Id
    @Column(name = "customer_id", length = 64)
    @Comment("고객사 PK")
    private String customerId;

    @Column(name = "customer_name", length = 200, nullable = false)
    @Comment("고객사명")
    private String customerName;

    @Column(name = "connected_company", length = 64)
    @Comment("연결된 판매사/파트너(예: 총판, 리셀러)")
    private String connectedCompany;

    @Column(name = "create_at")
    @Comment("생성일")
    private LocalDate createAt;

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getConnectedCompany() {
		return connectedCompany;
	}

	public void setConnectedCompany(String connectedCompany) {
		this.connectedCompany = connectedCompany;
	}

	public LocalDate getCreateAt() {
		return createAt;
	}

	public void setCreateAt(LocalDate createAt) {
		this.createAt = createAt;
	}
    
    
}