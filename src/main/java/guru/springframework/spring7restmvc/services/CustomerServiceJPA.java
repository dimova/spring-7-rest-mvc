package guru.springframework.spring7restmvc.services;

import guru.springframework.spring7restmvc.mappers.CustomerMapper;
import guru.springframework.spring7restmvc.model.CustomerDTO;
import guru.springframework.spring7restmvc.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by jt, Spring Framework Guru.
 */
@Service
@Primary
@RequiredArgsConstructor
public class CustomerServiceJPA implements CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public Optional<CustomerDTO> getCustomerById(UUID uuid) {
        return customerRepository.findById(uuid)
                .map(customerMapper::customerToCustomerDto);
    }

    @Override
    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(customerMapper::customerToCustomerDto)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerDTO saveNewCustomer(CustomerDTO customer) {
        return customerMapper.customerToCustomerDto(
                customerRepository.save(customerMapper.customerDtoToCustomer(customer))
        );
    }

    @Override
    public void updateCustomerById(UUID customerId, CustomerDTO customer) {
        customerRepository.findById(customerId).ifPresent(existingCustomer -> {
            existingCustomer.setName(customer.getName());
            customerRepository.save(existingCustomer);
        });
    }

    @Override
    public void deleteCustomerById(UUID customerId) {
        customerRepository.deleteById(customerId);
    }

    @Override
    public void patchCustomerById(UUID customerId, CustomerDTO customer) {
        customerRepository.findById(customerId).ifPresent(existingCustomer -> {
            if (customer.getName() != null && !customer.getName().isBlank()) {
                existingCustomer.setName(customer.getName());
            }
            customerRepository.save(existingCustomer);
        });
    }
}
