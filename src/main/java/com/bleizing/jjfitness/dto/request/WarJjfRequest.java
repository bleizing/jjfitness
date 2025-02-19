package com.bleizing.jjfitness.dto.request;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarJjfRequest implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3157047739141963393L;
	
	private String username;	//	dimasz_97@gmail.com
	private String password;	//	1693484551
	private String woName;
}
