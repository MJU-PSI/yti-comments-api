package fi.vm.yti.comments.api.entity;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "commentthread")
@XmlRootElement
public class CommentThread extends AbstractIdentifyableEntity {

    private String resourceUri;
    private Map<String, String> label;
    private Map<String, String> definition;
    private String proposedText;
    private String proposedStatus;
    private UUID userId;
    private LocalDateTime created;
    private Set<Comment> comments;
    private CommentRound commentRound;

    @Column(name = "resourceuri")
    public String getResourceUri() {
        return resourceUri;
    }

    public void setResourceUri(final String resourceUri) {
        this.resourceUri = resourceUri;
    }

    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "commentthread_label", joinColumns = @JoinColumn(name = "commentthread_id", referencedColumnName = "id"))
    @MapKeyColumn(name = "language")
    @Column(name = "label")
    @OrderColumn
    public Map<String, String> getLabel() {
        return label;
    }

    public void setLabel(final Map<String, String> label) {
        this.label = label;
    }

    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "commentthread_definition", joinColumns = @JoinColumn(name = "commentthread_id", referencedColumnName = "id"))
    @MapKeyColumn(name = "language")
    @Column(name = "definition")
    @OrderColumn
    public Map<String, String> getDefinition() {
        return definition;
    }

    public void setDefinition(final Map<String, String> definition) {
        this.definition = definition;
    }

    @Column(name = "proposedtext")
    public String getProposedText() {
        return proposedText;
    }

    public void setProposedText(final String proposedText) {
        this.proposedText = proposedText;
    }

    @Column(name = "proposedstatus")
    public String getProposedStatus() {
        return proposedStatus;
    }

    public void setProposedStatus(final String proposedStatus) {
        this.proposedStatus = proposedStatus;
    }

    @Column(name = "user_id")
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(final UUID userId) {
        this.userId = userId;
    }

    @Column(name = "created")
    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(final LocalDateTime created) {
        this.created = created;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "commentThread", cascade = CascadeType.ALL)
    public Set<Comment> getComments() {
        return comments;
    }

    public void setComments(final Set<Comment> comments) {
        this.comments = comments;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commentround_id", nullable = false, updatable = false)
    public CommentRound getCommentRound() {
        return commentRound;
    }

    public void setCommentRound(final CommentRound commentRound) {
        this.commentRound = commentRound;
    }
}
