import React, {Component} from 'react'
import {Button, Card, CardBody, CardHeader, CardText, Col, Row} from 'reactstrap'
import Talk from "./Talks";
import {graphql, compose} from 'react-apollo'
import gql from 'graphql-tag'

class Speaker extends Component {

    constructor() {
        super();

        this.state = {
            title: '',
            duration: ''
        }
    }

//tag::calling[]
    addNewTalk = () => {
        const {title, duration} = this.state;
        const {speaker} = this.props;

        this.setState({title: '', duration: ''});

        this.props.talkCreate({talk: {title, duration, speaker: {id: speaker.id}}})
          .then(({data}) => console.log('create response:', data))
          .catch((error) => console.log('there was an error sending the query', error));
    };

    deleteTalk = (id) => {
        this.props.talkDelete({id: id})
          .then(({data}) => console.log('delete response: ', data))
          .catch((error) => console.log('there was an error sending the query', error));
    };
//end::calling[]


    handleNewTalkChange = (event) => {
        const target = event.target;
        const value = target.type === 'checkbox' ? target.checked : target.value;
        const name = target.name;

        this.setState({
            [name]: value
        });
    }

    render() {
        const {speaker} = this.props;
        const {title, duration} = this.state;

        return <Card inverse color="info">
            <CardHeader>{speaker.firstName} {speaker.lastName}</CardHeader>
            <CardBody>
                <CardText>

                    <Row>
                        <ul style={{listStyle: 'none', width: '100%'}}>

                            <li>
                                <Row>
                                    <Col> <label>Title</label>
                                        <input type='text' name='title' value={title}
                                               onChange={this.handleNewTalkChange}/></Col>
                                    <Col><label>Duration</label>
                                        <input type="number" name="duration" value={duration}
                                               onChange={this.handleNewTalkChange}/></Col>
                                    <Col md="1"> <Button color="success" onClick={this.addNewTalk}>Add Talk</Button>
                                    </Col>
                                </Row>

                            </li>
                            <li>
                                <br/><hr/><br/>
                            </li>
                            {speaker.talks.map(t => <Talk key={t.id} talk={t}
                                                             delete={() => this.deleteTalk(t.id)}/>)}
                        </ul>
                    </Row>

                </CardText>

            </CardBody>
        </Card>
    }
}

//tag::mutations[]
const talkCreate = gql`
    mutation talkCreate($talk: TalkCreate!) {
        talkCreate(talk: $talk) {
            id
            title
            duration
        }
    }
`;

const talkDelete = gql`
    mutation talkDelete($id: Long!) {
        talkDelete(id: $id) {
            error
        }
    }
`;
//end::mutations[]

//tag::compose[]
const SpeakerWithMutations = compose(
  graphql(talkCreate, {
      props: ({mutate}) => ({
          talkCreate: ({talk}) =>
            mutate({
                variables: {talk},
            })
      })
  }),
  graphql(talkDelete, {
      props: ({mutate}) => ({
          talkDelete: ({id}) =>
            mutate({
                variables: {id},
            })
      })
  })
)(Speaker)

export default SpeakerWithMutations;
//end::compose[]