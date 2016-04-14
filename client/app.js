'use strict';

// tag::vars[]
require("../src/main/resources/public/main.css.scss");
const React = require('react');
const client = require('./client');

// end::vars[]

// tag::app[]
class App extends React.Component {

    constructor(props) {
        super(props);
        this.state = {employees: []};
    }

    componentDidMount() {
        client({method: 'GET', path: '/employees'}).done(response => {
            this.setState({employees: JSON.parse(response.raw.responseText).employees});
        });
    }

    render() {
        return (
            <EmployeeList employees={this.state.employees}/>
        )
    }
}
// end::app[]

// tag::employee-list[]
class EmployeeList extends React.Component {
    render() {
        var employees = this.props.employees.map(employee =>
            <Employee key={employee.firstName} employee={employee}/>
        );
        return (
            <table id="employee">
                <tr>
                    <th>First Name</th>
                    <th>Last Name</th>
                </tr>
                {employees}
            </table>
        )
    }
}
// end::employee-list[]

// tag::employee[]
class Employee extends React.Component {
    render() {
        return (
            <tr>
                <td>{this.props.employee.firstName}</td>
                <td>{this.props.employee.lastName}</td>
            </tr>
        )
    }
}
// end::employee[]

// tag::render[]
React.render(
    <App />,
    document.getElementById('app')
)
// end::render[]

