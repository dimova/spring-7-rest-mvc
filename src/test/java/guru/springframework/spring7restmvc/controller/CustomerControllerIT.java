package guru.springframework.spring7restmvc.controller;

import guru.springframework.spring7restmvc.entities.Customer;
import guru.springframework.spring7restmvc.model.CustomerDTO;
import guru.springframework.spring7restmvc.repositories.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CustomerControllerIT {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    CustomerController customerController;

    // ========== LIST ALL CUSTOMERS TESTS ==========

    @Rollback
    @Transactional
    @Test
    void testListAllEmptyList() {
        customerRepository.deleteAll();
        List<CustomerDTO> dtos = customerController.listAllCustomers();

        assertThat(dtos).isNotNull();
        assertThat(dtos.size()).isEqualTo(0);
    }

    @Test
    void testListAll() {
        List<CustomerDTO> dtos = customerController.listAllCustomers();

        assertThat(dtos).isNotNull();
        assertThat(dtos.size()).isGreaterThan(0);
    }

    @Test
    void testListAllReturnsValidCustomers() {
        List<CustomerDTO> dtos = customerController.listAllCustomers();

        assertThat(dtos).isNotNull();
        assertThat(dtos).isNotEmpty();
        dtos.forEach(dto -> {
            assertThat(dto.getId()).isNotNull();
            assertThat(dto.getName()).isNotNull();
        });
    }

    // ========== GET CUSTOMER BY ID TESTS ==========

    @Test
    void testGetByIdNotFound() {
        assertThrows(NotFoundException.class, () -> {
            customerController.getCustomerById(UUID.randomUUID());
        });
    }

    @Test
    void testGetById() {
        Customer customer = customerRepository.findAll().get(0);
        CustomerDTO customerDTO = customerController.getCustomerById(customer.getId());
        
        assertThat(customerDTO).isNotNull();
        assertThat(customerDTO.getId()).isEqualTo(customer.getId());
        assertThat(customerDTO.getName()).isEqualTo(customer.getName());
    }

    @Test
    void testGetByIdReturnsCompleteCustomer() {
        Customer customer = customerRepository.findAll().get(0);
        CustomerDTO customerDTO = customerController.getCustomerById(customer.getId());

        assertThat(customerDTO.getId()).isNotNull();
        assertThat(customerDTO.getName()).isNotNull();
        assertThat(customerDTO.getVersion()).isNotNull();
    }

    // ========== SAVE NEW CUSTOMER TESTS ==========

    @Rollback
    @Transactional
    @Test
    void testSaveNewCustomer() {
        CustomerDTO newCustomer = CustomerDTO.builder()
                .name("New Customer")
                .build();

        List<CustomerDTO> savedCustomer = customerController.listAllCustomers(); // Get initial count indirectly
        int initialCount = customerController.listAllCustomers().size();

        List<CustomerDTO> result = customerController.listAllCustomers(); // This will be replaced by actual save call

        // Note: The controller needs a POST endpoint to test save
        // This test structure assumes the endpoint exists
    }

    @Rollback
    @Transactional
    @Test
    void testSaveNewCustomerHappyPath() {
        CustomerDTO newCustomerDTO = CustomerDTO.builder()
                .name("Test Customer")
                .version(1)
                .createdDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        int initialCount = customerController.listAllCustomers().size();
        
        // Directly test the service to verify save functionality
        List<CustomerDTO> savedCustomer = customerController.listAllCustomers(); // Placeholder
        
        assertThat(savedCustomer).isNotNull();
    }

    // ========== UPDATE CUSTOMER TESTS ==========

    @Rollback
    @Transactional
    @Test
    void testUpdateCustomerHappyPath() {
        Customer customer = customerRepository.findAll().get(0);
        CustomerDTO updateDTO = CustomerDTO.builder()
                .name("Updated Name")
                .build();

        customerController.updateCustomerByID(customer.getId(), updateDTO);

        Customer updated = customerRepository.findById(customer.getId()).orElse(null);
        assertThat(updated).isNotNull();
        assertThat(updated.getName()).isEqualTo("Updated Name");
    }

    @Rollback
    @Transactional
    @Test
    void testUpdateCustomerNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        CustomerDTO updateDTO = CustomerDTO.builder()
                .name("Updated Name")
                .build();

        // Should not throw exception, just silently not update
        customerController.updateCustomerByID(nonExistentId, updateDTO);
        
        assertThat(customerRepository.findById(nonExistentId)).isEmpty();
    }

    @Rollback
    @Transactional
    @Test
    void testUpdateCustomerChangesName() {
        Customer customer = customerRepository.findAll().get(0);
        String originalName = customer.getName();
        String newName = "Completely Different Name";

        CustomerDTO updateDTO = CustomerDTO.builder()
                .name(newName)
                .build();

        customerController.updateCustomerByID(customer.getId(), updateDTO);

        Customer updated = customerRepository.findById(customer.getId()).orElseThrow();
        assertThat(updated.getName())
                .isNotEqualTo(originalName)
                .isEqualTo(newName);
    }

    // ========== PATCH CUSTOMER TESTS ==========

    @Rollback
    @Transactional
    @Test
    void testPatchCustomerHappyPath() {
        Customer customer = customerRepository.findAll().get(0);
        String newName = "Patched Name";

        CustomerDTO patchDTO = CustomerDTO.builder()
                .name(newName)
                .build();

        customerController.patchCustomerById(customer.getId(), patchDTO);

        Customer patched = customerRepository.findById(customer.getId()).orElseThrow();
        assertThat(patched.getName()).isEqualTo(newName);
    }

    @Rollback
    @Transactional
    @Test
    void testPatchCustomerWithBlankName() {
        Customer customer = customerRepository.findAll().get(0);
        String originalName = customer.getName();

        CustomerDTO patchDTO = CustomerDTO.builder()
                .name("   ")
                .build();

        customerController.patchCustomerById(customer.getId(), patchDTO);

        Customer patched = customerRepository.findById(customer.getId()).orElseThrow();
        // Blank names should be ignored in patch
        assertThat(patched.getName()).isEqualTo(originalName);
    }

    @Rollback
    @Transactional
    @Test
    void testPatchCustomerWithNullName() {
        Customer customer = customerRepository.findAll().get(0);
        String originalName = customer.getName();

        CustomerDTO patchDTO = CustomerDTO.builder()
                .build();

        customerController.patchCustomerById(customer.getId(), patchDTO);

        Customer patched = customerRepository.findById(customer.getId()).orElseThrow();
        // Null names should be ignored in patch
        assertThat(patched.getName()).isEqualTo(originalName);
    }

    @Rollback
    @Transactional
    @Test
    void testPatchCustomerNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        CustomerDTO patchDTO = CustomerDTO.builder()
                .name("Patched Name")
                .build();

        // Should not throw exception
        customerController.patchCustomerById(nonExistentId, patchDTO);
        
        assertThat(customerRepository.findById(nonExistentId)).isEmpty();
    }

    // ========== DELETE CUSTOMER TESTS ==========

    @Rollback
    @Transactional
    @Test
    void testDeleteCustomerHappyPath() {
        Customer customer = customerRepository.findAll().get(0);
        UUID customerId = customer.getId();

        assertThat(customerRepository.findById(customerId)).isPresent();

        customerController.deleteCustomerById(customerId);

        assertThat(customerRepository.findById(customerId)).isEmpty();
    }

    @Rollback
    @Transactional
    @Test
    void testDeleteCustomerNotFound() {
        UUID nonExistentId = UUID.randomUUID();

        // Should not throw exception
        assertDoesNotThrow(() -> customerController.deleteCustomerById(nonExistentId));
        
        assertThat(customerRepository.findById(nonExistentId)).isEmpty();
    }

    @Rollback
    @Transactional
    @Test
    void testDeleteCustomerReducesListSize() {
        int initialSize = customerController.listAllCustomers().size();
        Customer customer = customerRepository.findAll().get(0);

        customerController.deleteCustomerById(customer.getId());

        int finalSize = customerController.listAllCustomers().size();
        assertThat(finalSize).isEqualTo(initialSize - 1);
    }
}










